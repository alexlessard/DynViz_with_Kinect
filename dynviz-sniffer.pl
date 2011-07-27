#!/usr/local/bin/perl
# $Id$

$| = 1;

use IO::Socket::INET;
use Net::Pcap qw(:functions);
use Getopt::Long;

use Geo::IP::PurePerl;

use warnings;
use strict;

my @servers = qw( <dynviz receiver address> );
my $port = '24642';
my @networks;
my $iface = 'span0';
my ($site) = (`hostname` =~ /^<server name>-(...)/);
my $vlan = undef;
my $geoipdb = 'geoip/GeoLiteCity.dat';
my $pqlen = '20';

GetOptions(
 	'servers=s'	=> \@servers,
	'port=i'	=> \$port,
	'interface=s'	=> \$iface,
	'network=s'	=> \@networks,
	'site=s'	=> \$site,
	'vlan=i'	=> \$vlan,
	'geoipdb=s'	=> \$geoipdb,
	'pqlen=i'	=> \$pqlen,

) || exit;

foreach my $network (@networks) {
	$network =~ /^(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})\/(\d+),(\d\.\d+)$/;
	my ($net,$mask,$prob) = (inet_aton($1),bitmask($2),$3);
	$net = $net & $mask;
	$network = [ $net, $mask, $prob ];
}

my $srcoffset = 26;
my $dstoffset = 30;
$srcoffset += 4 if defined($vlan);
$dstoffset += 4 if defined($vlan);

# GeoIP Variables
my $gi = Geo::IP::PurePerl->open($geoipdb, GEOIP_STANDARD) || die "Failed to open GeoIP database\n";
my $radius = 800;
my $pi = 3.14159265358979;

# Packet and Socket Queue Variables
my @packetqueue;
my @sockets;

foreach my $server (@servers) {
	# Setup UDP socket for sending info to DynViz
	my $sock = IO::Socket::INET->new(
		PeerPort => $port,
		PeerAddr => $server,
		LocalPort => $port,
		ReusePort => 1,
		Proto => 'udp',
	) || die "Can't open udp socket: $@\n";
	push(@sockets,$sock);
}

# Setup Pcap to capture packets
my $err = '';
my $pcap = pcap_open_live($iface, 68, 1, 0, \$err)
	or die "Failed to open device $iface: $err\n";

my @filter;
push @filter, "vlan $vlan && " if defined $vlan;
push @filter, 'udp dst port 53';
push @filter, ' and ( ' if @networks > 0;
my @filter_nets;
foreach my $net ( @networks ) {
	my ($net,$mask) = @$net;
	$net = inet_ntoa($net);
	$mask = inet_ntoa($mask);
	print "Adding $net/$mask to filter.\n";
	push @filter_nets, "dst net $net mask $mask";
}
push @filter, join(' or ', @filter_nets);
push @filter, ' )' if @networks > 0;
my $filter_str = join('',@filter);

my ($net,$mask);
pcap_lookupnet($iface,\$net,\$mask,\$err);

my $filter = '';
pcap_compile($pcap, \$filter, $filter_str, 1, \$mask);
pcap_setfilter($pcap,$filter);

pcap_loop($pcap, -1, \&process_packet, '');

sub process_packet {
	my (undef, $header, $packet) = @_;

	# Extract the destination address, look it up in @networks
	my $dstip = substr $packet, $dstoffset, 4;

	my @nets = grep { in_network($_,$dstip) } @networks;
	
	if (@nets == 0) {
		print STDERR "WARNING: Packet matched no network, ";
		print STDERR "destination ",inet_ntoa($dstip),"\n";
		return;
	}

	my $prob = $nets[0]->[2];

	# If the prob is < 1, see if we should ignore it
	if ( $prob < 1 ) {
		return unless rand() < $prob;
	}

	# Otherwise, send it off the visualizer
	my $srcip = substr $packet, $srcoffset, 4;

	my $srcip_dotquad = ip_to_dotted($srcip);
	my $dstip_dotquad = ip_to_dotted($dstip);

	my @r = $gi->get_city_record($srcip_dotquad);
	if (@r) {
		my $lat	= $r[6];
		my $lon	= $r[7];

		my $rlat	= deg_to_rad($lat);
		my $rlon	= deg_to_rad(-$lon);

		my $outX	=  $radius * cos($rlon) * cos($rlat);
		my $outY	= -$radius * sin($rlat);
		my $outZ	=  $radius * cos($rlat) * sin($rlon);

		print "$outX\n";

		# Set precision to 4 decimal points
		$outX = sprintf("%.4f", $outX);
		$outY = sprintf("%.4f", $outY);
		$outZ = sprintf("%.4f", $outZ);

		print "$outX\n";

		my $packet = "$outX\t$outY\t$outZ\t$dstip_dotquad\t$site";

		push (@packetqueue, $packet);

		if (scalar @packetqueue > $pqlen) {
			foreach my $sock (@sockets) {
				$sock->send(join("\n", @packetqueue));
			}

			@packetqueue = ();
		}
	}
}

sub bitmask {
	return pack('B*', "1"x$_[0] . "0"x(32-$_[0]));
}

sub in_network {
	my ($netref,$ip) = @_;
	$ip = $ip & $netref->[1];
	my $res = unpack('I*',$ip ^ $netref->[0]);
	return 1 if $res == 0;
	return 0;
}

sub ip_to_dotted { join('.', unpack('C4', shift)) }
sub deg_to_rad	{ ($_[0]/180) * $pi }
