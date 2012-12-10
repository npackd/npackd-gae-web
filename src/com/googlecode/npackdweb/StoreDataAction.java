package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

public class StoreDataAction extends Action {
	private static class DiscoveryInfo {
		private String package_;
		private String url;
		private String re;
		private String download;

		public DiscoveryInfo(String package_, String url, String re,
		        String download) {
			this.url = url;
			this.re = re;
			this.download = download;
			this.package_ = package_;
		}
	}

	public StoreDataAction() {
		super("^/store-data$");
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
	        throws IOException {
		List<DiscoveryInfo> dis = new ArrayList<DiscoveryInfo>();
		dis
		        .add(new DiscoveryInfo(
		                "org.areca-backup.ArecaBackup",
		                "http://sourceforge.net/api/file/index/project-id/171505/mtime/desc/limit/20/rss",
		                "areca-([\\d\\.]+)-windows-jre32.zip",
		                "http://downloads.sourceforge.net/project/areca/areca-stable/areca-${{actualVersion}}/areca-${{actualVersion}}-windows-jre32.zip"));
		dis
		        .add(new DiscoveryInfo(
		                "org.areca-backup.ArecaBackup64",
		                "http://sourceforge.net/api/file/index/project-id/171505/mtime/desc/limit/20/rss",
		                "areca-([\\d\\.]+)-windows-jre32.zip",
		                "http://downloads.sourceforge.net/project/areca/areca-stable/areca-${{actualVersion}}/areca-${{actualVersion}}-windows-jre64.zip"));
		dis
		        .add(new DiscoveryInfo(
		                "org.boost.Boost",
		                "http://sourceforge.net/api/file/index/project-id/7586/mtime/desc/limit/120/rss",
		                "/boost-binaries/([\\d\\.]+)/",
		                "http://downloads.sourceforge.net/project/boost/boost/${{actualVersion}}/boost_${{actualVersionWithUnderscores}}.zip"));
		dis
		        .add(new DiscoveryInfo(
		                "org.mozilla.Firefox",
		                "http://www.mozilla.org/en-US/firefox/all.html",
		                "<td class=\"curVersion\" >([\\d\\.]+)</td>",
		                "http://download.mozilla.org/?product=firefox-${{actualVersion}}&os=win&lang=en-US"));
		dis
		        .add(new DiscoveryInfo("net.sourceforge.audacity.Audacity",
		                "http://audacity.sourceforge.net/?lang=en",
		                "<h3>Download Audacity ([\\d\\.]+)</h3>",
		                "http://audacity.googlecode.com/files/audacity-win-${{actualVersion}}.exe"));
		dis
		        .add(new DiscoveryInfo(
		                "com.clamwin.ClamWin",
		                "http://sourceforge.net/api/file/index/project-id/105508/mtime/desc/limit/20/rss",
		                "\\[/clamwin/[\\d\\.]+/clamwin-([\\d\\.]+)-setup.exe\\]",
		                "http://downloads.sourceforge.net/project/clamwin/clamwin/${{actualVersion}}/clamwin-${{actualVersion}}-setup.exe"));
		dis
		        .add(new DiscoveryInfo(
		                "se.haxx.curl.CURL",
		                "http://curl.haxx.se/",
		                ">The most recent stable version of curl is version ([\\d\\.]+)<",
		                "http://curl.haxx.se/gknw.net/${{actualVersion}}/dist-w32/curl-${{actualVersion}}-rtmp-ssh2-ssl-sspi-zlib-idn-static-bin-w32.zip"));
		dis
		        .add(new DiscoveryInfo(
		                "se.haxx.curl.CURL64",
		                "http://curl.haxx.se/",
		                ">The most recent stable version of curl is version ([\\d\\.]+)<",
		                "http://curl.haxx.se/gknw.net/${{actualVersion}}/dist-w64/curl-${{actualVersion}}-rtmp-ssh2-ssl-sspi-zlib-winidn-static-bin-w64.7z"));
		dis
		        .add(new DiscoveryInfo(
		                "com.ghostscript.GhostScriptInstaller",
		                "http://www.ghostscript.com/download/",
		                ">Ghostscript ([\\d\\.]+)<",
		                "http://downloads.ghostscript.com/public/gs${{actualVersionWithoutDots}}w32.exe"));
		dis
		        .add(new DiscoveryInfo(
		                "com.ghostscript.GhostScript64Installer",
		                "http://www.ghostscript.com/download/",
		                ">Ghostscript ([\\d\\.]+)<",
		                "http://downloads.ghostscript.com/public/gs${{actualVersionWithoutDots}}w64.exe"));
		dis
		        .add(new DiscoveryInfo(
		                "net.java.dev.glassfish.GlassFishWebProfile",
		                "http://glassfish.java.net/",
		                "Open Source Edition ([\\d\\.]+)",
		                "http://download.java.net/glassfish/${{actualVersion}}/release/glassfish-${{actualVersion}}-web-ml.zip"));
		dis
		        .add(new DiscoveryInfo(
		                "org.gnucash.GNUCash",
		                "http://sourceforge.net/api/file/index/project-id/192/mtime/desc/limit/20/rss",
		                "\\[/gnucash \\(stable\\)/[\\d\\.]+/gnucash-([\\d\\.]+)-setup.exe\\]",
		                "http://downloads.sourceforge.net/project/gnucash/gnucash%20%28stable%29/${{actualVersion}}/gnucash-${{actualVersion}}-setup.exe"));
		dis.add(new DiscoveryInfo("org.izarc.IZArc",
		        "http://www.izarc.org/download.html", "<b>([\\d\\.]+)</b>",
		        "http://www.izarc.org/download/IZArcInstall.exe"));
		dis
		        .add(new DiscoveryInfo(
		                "org.pdfforge.PDFCreator",
		                "http://www.pdfforge.org/download",
		                "Download PDFCreator ([\\d\\.]+)",
		                "http://blue.download.pdfforge.org/pdfcreator/${{actualVersion}}/PDFCreator-${{actualVersionWithUnderscores}}_setup.exe"));
		dis
		        .add(new DiscoveryInfo(
		                "net.icsharpcode.SharpDevelop",
		                "https://sourceforge.net/api/file/index/project-id/17610/mtime/desc/limit/120/rss",
		                "http://sourceforge.net/projects/sharpdevelop/files/SharpDevelop.+/SharpDevelop_([\\d\\.]+)_Setup.msi",
		                "http://sourceforge.net/projects/sharpdevelop/files/SharpDevelop%204.x/${{version2Parts}}/SharpDevelop_${{actualVersion}}_Setup.msi"));
		dis
		        .add(new DiscoveryInfo("org.apache.subversion.Subversion",
		                "http://www.sliksvn.com/en/download", "> ([\\d\\.]+)<",
		                "http://www.sliksvn.com/pub/Slik-Subversion-${{actualVersion}}-win32.msi"));
		dis
		        .add(new DiscoveryInfo("org.apache.subversion.Subversion64",
		                "http://www.sliksvn.com/en/download", "> ([\\d\\.]+)<",
		                "http://www.sliksvn.com/pub/Slik-Subversion-${{actualVersion}}-x64.msi"));
		dis
		        .add(new DiscoveryInfo(
		                "org.apache.tomcat.Tomcat",
		                "http://tomcat.apache.org/download-70.cgi",
		                "<strong>([\\d\\.]+)</strong>",
		                "http://ftp.halifax.rwth-aachen.de/apache/tomcat/tomcat-7/v${{actualVersion}}/bin/apache-tomcat-${{actualVersion}}.exe"));
		dis
		        .add(new DiscoveryInfo(
		                "org.apache.tomcat.Tomcat64",
		                "http://tomcat.apache.org/download-70.cgi",
		                "<strong>([\\d\\.]+)</strong>",
		                "http://ftp.halifax.rwth-aachen.de/apache/tomcat/tomcat-7/v${{actualVersion}}/bin/apache-tomcat-${{actualVersion}}.exe"));
		dis
		        .add(new DiscoveryInfo(
		                "net.sourceforge.winrun4j.WinRun4j",
		                "http://sourceforge.net/api/file/index/project-id/195634/mtime/desc/limit/20/rss",
		                "winrun4J-([\\d\\.]+)\\.zip",
		                "http://downloads.sourceforge.net/project/winrun4j/winrun4j/${{actualVersion}}/winrun4J-${{actualVersion}}.zip"));
		dis
		        .add(new DiscoveryInfo(
		                "net.winscp.WinSCP",
		                "http://winscp.net/eng/download.php",
		                ">WinSCP ([\\d\\.]+)<",
		                "http://downloads.sourceforge.net/project/winscp/WinSCP/${{actualVersion}}/winscp${{actualVersionWithoutDots}}.zip"));
		dis
		        .add(new DiscoveryInfo(
		                "org.wireshark.Wireshark",
		                "http://www.wireshark.org/download.html",
		                "The current stable release of Wireshark is ([\\d\\.]+)\\.",
		                "http://wiresharkdownloads.riverbed.com/wireshark/win32/Wireshark-win32-${{actualVersion}}.exe"));
		dis
		        .add(new DiscoveryInfo(
		                "org.wireshark.Wireshark64",
		                "http://www.wireshark.org/download.html",
		                "The current stable release of Wireshark is ([\\d\\.]+)\\.",
		                "http://wiresharkdownloads.riverbed.com/wireshark/win64/Wireshark-win64-${{actualVersion}}.exe"));
		dis
		        .add(new DiscoveryInfo(
		                "org.xapian.XapianCore",
		                "http://xapian.org/",
		                "latest stable version is ([\\d\\.]+)<",
		                "http://oligarchy.co.uk/xapian/${{actualVersion}}/xapian-core-${{actualVersion}}.tar.gz"));

		Objectify ob = NWUtils.getObjectify();
		List<Package> packages = new ArrayList<Package>();
		for (DiscoveryInfo d : dis) {
			Package p = ob.get(new Key<Package>(Package.class, d.package_));
			p.discoveryPage = d.url;
			p.discoveryRE = d.re;
			p.discoveryURLPattern = d.download;
			packages.add(p);
		}
		ob.put(packages);
		return null;
	}
}
