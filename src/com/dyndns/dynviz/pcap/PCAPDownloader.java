package com.dyndns.dynviz.pcap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.dyndns.dynviz.prop.Props;

public class PCAPDownloader {

	private static final SimpleDateFormat HTTP_DATE_FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

	private final String url;
	private final OnFileLoadedListener listener;
	private int interval;

	private Date lastModifiedDate;
	private String lastETag;
	private File loadedFile;

	private Thread downloadThread;

	public PCAPDownloader(String url, OnFileLoadedListener listener) {
		if (url == null || listener == null)
			throw new NullPointerException();

		this.url = url;
		this.listener = listener;

		// initializing current state, notifying listener with already loaded file (if any)
		initLastDownloadedFile();
	}

	public void startLoading(int interval) {
		if (interval == 0 && loadedFile != null)
			return;

		this.interval = interval;

		if (downloadThread != null)
			return; // already started

		downloadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				threadRun();
			}
		});
		downloadThread.start();
	}

	private void threadRun() {
		while (true) {
			if (interval == 0 && loadedFile != null)
				break;

			long downloadStart = System.currentTimeMillis();

			try {
				downloadInBackground();
			} catch (Exception e) {
				e.printStackTrace(); // TODO: show only message
				loadedFile = null;
			}

			if (loadedFile == null)
				listener.onFileLoaded(Props.string(Props.PCAP_FILE));

			long sleepTime;
			if (interval == 0) {
				sleepTime = 5 * 60 * 1000; // retrying in 5 minutes
			} else {
				sleepTime = interval - (System.currentTimeMillis() - downloadStart);
			}
			System.out.println((sleepTime / 1000) + " seconds untill next PCAP file loading");

			try {
				Thread.sleep((sleepTime < 0 ? 0 : sleepTime) + 1000);
			} catch (InterruptedException e) {
				System.err.println("PCAP downloader thread is interrupted");
			}
		}

		downloadThread = null;
	}

	private void downloadInBackground() throws IOException {
		System.out.println("Starting loading pcap file from: " + url);
		HttpClient client = new DefaultHttpClient();
		HttpUriRequest req = new HttpGet(url);
		if (lastModifiedDate != null)
			req.addHeader("If-Modified-Since", formatHttpDate(lastModifiedDate));
		if (lastETag != null)
			req.addHeader("If-None-Match", lastETag);

		HttpResponse resp = client.execute(req);
		HttpEntity entity = resp.getEntity();
		try {
			int statusCode = resp.getStatusLine().getStatusCode();
			if (statusCode == 304) {
				System.out.println("Pcap file was not modified, no reloading needed");
				return;
			}
			if (statusCode / 100 != 2) {
				throw new IOException("Server retruns wrong status code: " + statusCode);
			}

			Date lastModifiedDate = getLastModifiedDate(resp);
			String etag = getETag(resp);

			File dest = getPcapFileForDate(lastModifiedDate);
			if (dest.exists()) {
				System.out.println("File already exists: " + dest.getAbsolutePath());
				return; // already have file for this date
			}

			System.out.println("Downloading file to: " + dest.getAbsolutePath());

			saveToFile(entity, dest);
			saveETagForDate(etag, lastModifiedDate);

			initLastDownloadedFile(); // reloading current state, notifying listener with loaded file

			if (loadedFile == null) {
				System.out.println("Pcap file not loaded");
			} else {
				System.out.println("Pcap file successfully loaded: " + loadedFile.getAbsolutePath());
			}
		} finally {
			if (entity != null)
				EntityUtils.consumeQuietly(entity);
		}
	}

	private void saveToFile(HttpEntity entity, File dest) throws IOException {
		if (entity == null)
			throw new IOException("Server returned no content");

		File tempFile = getTempFile();
		InputStream in = null;
		OutputStream out = null;
		try {
			// System.out.println("Saving response into temporary file: " + tempFile.getAbsolutePath());
			in = entity.getContent();
			out = FileUtils.openOutputStream(tempFile);
			IOUtils.copy(in, out);
			dest.getParentFile().mkdirs();
		} catch (IOException e) {
			FileUtils.deleteQuietly(tempFile);
			throw e;
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}

		// System.out.println("Moving temporary file from: " + tempFile.getAbsolutePath() + " to: " + dest.getAbsolutePath());
		tempFile.renameTo(dest);
	}

	private void initLastDownloadedFile() {
		File maxFile = null;
		Date maxDate = null;
		Date tmpDate = null;

		for (File file : FileUtils.listFiles(getPcapDir(), null, false)) {
			if (!file.getName().endsWith(".pcap")) continue;
			tmpDate = getLastModifiedDateFromFileName(file.getName());
			if (tmpDate == null) {
				// Wrong file, deleting
				FileUtils.deleteQuietly(file);
			} else if (maxDate == null || maxDate.before(tmpDate)) {
				if (maxFile != null) {
					// Newer file found, deleting old one
					FileUtils.deleteQuietly(maxFile);
					removeETagForDate(maxDate);
				}
				maxDate = tmpDate;
				maxFile = file;
			} else {
				// Newer file found, deleting old one
				FileUtils.deleteQuietly(file);
				System.out.println(3);
			}
		}

		lastModifiedDate = maxDate;
		lastETag = lastModifiedDate == null ? null : getETagForDate(lastModifiedDate);
		loadedFile = maxFile;

		listener.onFileLoaded(loadedFile == null ? null : loadedFile.getAbsolutePath());
	}

	private File getPcapFileForDate(Date lastModifiedDate) {
		return new File(getPcapDir(), "pcap-" + lastModifiedDate.getTime() + ".pcap");
	}

	private Date getLastModifiedDateFromFileName(String name) {
		if (name == null || name.length() == 0)
			return null;
		String dateStr = name.replace("pcap-", "").replace(".pcap", "");
		try {
			return new Date(Long.valueOf(dateStr));
		} catch (NumberFormatException e) {
			System.err.println("Can't parse date from file name: " + dateStr + " (" + name + ")");
			return null;
		}
	}

	private File getETagFile(Date lastModifiedDate) {
		return new File(getPcapDir(), "pcap-" + lastModifiedDate.getTime() + ".etag");
	}

	private void saveETagForDate(String etag, Date lastModifiedDate) {
		try {
			if (etag == null)
				etag = "null";
			File file = getETagFile(lastModifiedDate);
			file.getParentFile().mkdirs();
			FileUtils.write(file, etag, Charsets.UTF_8);
		} catch (IOException e) {
			System.err.println("Can't save etag into file: " + e.getMessage());
		}
	}

	private String getETagForDate(Date lastModifiedDate) {
		File file = getETagFile(lastModifiedDate);
		if (!file.exists())
			return null;
		try {
			String etag = FileUtils.readFileToString(file, Charsets.UTF_8);
			if (etag != null && etag.equals("null"))
				return null;
			return etag;
		} catch (IOException e) {
			System.err.println("Can't read ETag from file: " + e.getMessage());
			return null;
		}
	}

	private void removeETagForDate(Date lastModifiedDate) {
		FileUtils.deleteQuietly(getETagFile(lastModifiedDate));
	}

	private File getDynVizDir() {
		File dir = new File(FileUtils.getTempDirectory(), "DynViz");
		dir.mkdirs();
		return dir;
	}

	private File getPcapDir() {
		File dir = new File(getDynVizDir(), "pcap-" + url.hashCode());
		dir.mkdirs();
		return dir;
	}

	private File getTempFile() {
		return new File(getDynVizDir(), "pcap-" + System.nanoTime() + ".tmp");
	}

	private static Date getLastModifiedDate(HttpResponse resp) {
		Header lastModifiedHeader = resp.getFirstHeader("Last-Modified");
		String lastModifiedValue = lastModifiedHeader == null ? null : lastModifiedHeader.getValue();
		Date lastModifiedDate = parseHttpDate(lastModifiedValue);
		return lastModifiedDate == null ? new Date() : lastModifiedDate;
	}

	private static String formatHttpDate(Date date) {
		HTTP_DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
		return HTTP_DATE_FORMATTER.format(date);
	}

	private static String getETag(HttpResponse resp) {
		Header etagHeader = resp.getFirstHeader("ETag");
		return etagHeader == null ? null : etagHeader.getValue();
	}

	private static Date parseHttpDate(String str) {
		try {
			if (str == null || str.length() == 0)
				return null;
			return HTTP_DATE_FORMATTER.parse(str);
		} catch (ParseException e) {
			System.err.println("Can't parse http date: " + str);
			return null;
		}
	}

	public static interface OnFileLoadedListener {
		void onFileLoaded(String filePath);
	}

}
