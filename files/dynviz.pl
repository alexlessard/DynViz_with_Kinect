use strict;

use Date::Parse;
use Date::Format;
use Date::Language;

use Time::HiRes qw(gettimeofday tv_interval);

use Geo::IP::PurePerl;
use Net::IP;
use IO::Socket;

use Data::Dumper;

my @anycast = (
	new Net::IP('208.78.70/27'),
	new Net::IP('208.78.71/27'),
	new Net::IP('204.13.250/27'),
	new Net::IP('204.13.251/27')
);
my $unicast = 1;

my $pi = 3.14159265358979;
my $radius = 800;

my $gi = Geo::IP::PurePerl->open($ENV{'USERPROFILE'} . '.', GEOIP_STANDARD);

my $addrs_file = $ENV{'USERPROFILE'} .'send.txt';

open(F, $addrs_file) or die("Unable to open file addrs.txt");
my @addrs = <F>;
close(F);

my %socks;

#my $UDP_CLIENT_NAME = 'oxygen.mht.dyndns.com';
my $UDP_CLIENT_NAME = '172.16.30.141';
my $UDP_CLIENT_PORT = 12321;

my $UDP_SERVER_PORT = 24642;

my $MAX_TO_READ = 1024;

my $lang = Date::Language->new('English');

my $UDP_SERVER = new IO::Socket::INET->new(LocalPort => $UDP_SERVER_PORT, Proto => 'udp')
	or die "Couldn't be a udp server on port $UDP_SERVER_PORT : $@\n";

#my $UDP_CLIENT =

foreach my $addr (@addrs) {
	$socks{$addr} = IO::Socket::INET->new(PeerPort => $UDP_CLIENT_PORT, Proto => 'udp', PeerAddr => $addr)
		or die "Couldn't be a udp client on port $UDP_CLIENT_PORT : $@\n";
}

BEGIN { $| = 1 }
print "PERL: Awaiting UDP messages on port $UDP_SERVER_PORT\n";

my %servers;

#my $qps = 0;
my $qps_uni = 0;
my $qps_any = 0;

my $qps_time_start = [gettimeofday];
my $ua_time_start =  $qps_time_start;

my @qps_uni_history;
my @qps_any_history;

my $newmsg;

my $stop = 0;


my $silent = 0;

foreach my $i (0 .. $#ARGV) {
	$ARGV[$i] =~ /(\w)/;

	$silent = 1 if ($1 eq 's');
}

$silent = 1;
print "entering\n";

while ($UDP_SERVER->recv($newmsg, $MAX_TO_READ) && !$stop) {
	print "got one\n";
	if (length($newmsg) == 11) {
		my $bin_ip_srce = substr($newmsg, 0, 4);
		my $bin_ip_dest = substr($newmsg, 4, 4);
		my $resolver = substr($newmsg, 8, 3);

		$servers{$resolver} ||= 0;
		$servers{$resolver}++;

		my $ip_srce =	ip_to_dotted($bin_ip_srce);
		my $ip_dest =	ip_to_dotted($bin_ip_dest);

		my $packet_unicast = 1;
		foreach my $any (@anycast) {
			if ($any->overlaps(new Net::IP($ip_dest))==$IP_B_IN_A_OVERLAP) {
				$packet_unicast = 0;
				last;
			}
		}

		if ($packet_unicast) {
			$qps_uni++;
		} else {
			$qps_any++;
		}

		my $qps = $packet_unicast ? $qps_uni : $qps_any;

		my $now = [gettimeofday];
		my $qps_time_elapsed = tv_interval ( $qps_time_start, $now);

		if ($qps_time_elapsed > .05) {
			#if ($unicast) {
			#	$qps *= 10;
			#}

			if ($packet_unicast) {
				for my $i (0 .. $#qps_uni_history) {
					@qps_uni_history[$i] += $qps_uni;
				}

				unshift(@qps_uni_history, $qps_uni);
				splice(@qps_uni_history, 20);

				if ($#qps_uni_history >= 19 && $unicast == 1) {
					#print "PERL: Unicast QPS: $qps_uni_history[19]\n";

					foreach my $sock (keys %socks) {
						$socks{$sock}->send("q\t$qps_uni_history[19]");
					}
					#$UDP_CLIENT->send("q\t$qps_uni_history[19]");
					$qps_time_start = $now;
				}

				$qps_uni = 0;
			} else {
				for my $i (0 .. $#qps_any_history) {
					@qps_any_history[$i] += $qps_any;
				}

				unshift(@qps_any_history, $qps_any);
				splice(@qps_any_history, 20);

				if ($#qps_any_history >= 19 && $unicast == 0) {
					#print "PERL: Anycast QPS: $qps_any_history[19]\n";
					foreach my $sock (keys %socks) {
						$socks{$sock}->send("q\t$qps_any_history[19]");
					}
					#$UDP_CLIENT->send("q\t$qps_any_history[19]");
					$qps_time_start = $now;
				}

				$qps_any = 0;
			}

		}

		my $ua_time_elapsed = tv_interval ( $ua_time_start, $now);

		if ($ua_time_elapsed > 1.0) {
			if ($unicast) {
				#$UDP_CLIENT->send('u');
				foreach my $sock (keys %socks) {
					$socks{$sock}->send('u');
				}
			} else {
				#$UDP_CLIENT->send('a');
				foreach my $sock (keys %socks) {
					$socks{$sock}->send('a');
				}
			}

			$ua_time_start = $now;

			unless ($silent) {

				print("QPS -\t");

				foreach my $server (sort keys %servers) {
					print("$server\t");
				}

				print "\n\t";

				foreach my $server (sort keys %servers) {
					print("$servers{$server}\t");
					$servers{$server} = 0;
				}

				print("\n\n");
			}
		}


#		if ($packet_unicast == 0) {
#			print("Anycast: ($ip_srce, $ip_dest, $resolver)\n");
#		} else {
#			print("Unicast: ($ip_srce, $ip_dest, $resolver)\n");
#		}


		#if ($packet_unicast == $unicast)
		#if ($ip_dest eq '128.121.146.100')
		{
			my @r = $gi->get_city_record($ip_srce);

			if (@r) {
				my $lat	= $r[6];
				my $lon	= $r[7];

				my $rlat	= deg_to_rad($lat);
				my $rlon	= deg_to_rad(-$lon);

				my $outX	=  $radius * cos($rlon) * cos($rlat);
				my $outY	= -$radius * sin($rlat);
				my $outZ	=  $radius * cos($rlat) * sin($rlon);

				my $packet = "$outX\t$outY\t$outZ\t1\t$resolver";

				#$UDP_CLIENT->send($packet);
				foreach my $sock (keys %socks) {
					$socks{$sock}->send($packet);
				}
			}
		}

	} elsif (length($newmsg)==1) {
		if ($newmsg eq 'u') {
			$unicast = !$unicast;
		}
	} elsif (length($newmsg)==4 && $newmsg eq 'stop') {
		$stop = 1;
		print "PERL: Received STOP message.\n";
	}# else {
	#	print "INVALID PACKET RECEIVED\n";
	#}
}

print "PERL: Exiting.\n";

sub deg_to_rad	{ ($_[0]/180) * $pi }
sub round {my($number) = shift; return int($number + .5);}
sub ip_to_dotted {  join('.', unpack('C4', shift)) }
