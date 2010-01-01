package rath.tools.web.smap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;


/**
 * 
 * @author rath
 * @version 2010/01/01 
 */
public class SmapFilter {
	
	private File workDirectory = null;
	private Pattern patternNumber = Pattern.compile("\\d+");
	
	private Map<String, Map<Integer, SmapFile>> smapInfo = new HashMap<String, Map<Integer, SmapFile>>();
	
	public SmapFilter() {
		
	}
	
	public void setWorkDirectory( ServletContext ctx ) {
		setWorkDirectory(ctx, "localhost");
	}
	
	public void setWorkDirectory( ServletContext ctx, String hostname ) {
		if( System.getProperties().containsKey("catalina.base") ) 
			this.workDirectory = new File(System.getProperty("catalina.base"), 
				String.format("work/Catalina/%s/_", hostname));
		if( System.getProperties().containsKey("resin.home") ) 
			this.workDirectory = new File(ctx.getRealPath("/WEB-INF/work"));
	}
	
	public void setWorkDirectory(File workDirectory) {
		this.workDirectory = workDirectory;
	}
	
	public File getWorkDirectory() {
		return workDirectory;
	}
	
	public void filterException( Throwable e ) throws IOException {
		StackTraceElement[] ste = filterStackTraces(e.getStackTrace());
		e.setStackTrace(ste);
	}
	
	public StackTraceElement[] filterStackTraces( StackTraceElement[] in ) throws IOException {
		StackTraceElement[] ret = new StackTraceElement[in.length];
		for(int i=0; i<in.length; i++) {
			if( in[i].getClassName().startsWith("_jsp") ||
				in[i].getClassName().startsWith("org.apache.jsp.") ) {
				ret[i] = filterTraceElementImpl(in[i]);
			} else {
				ret[i] = in[i];
			}
		}
		return ret;
	}

	protected StackTraceElement filterTraceElementImpl(StackTraceElement ste) throws IOException {
		// jsp 소스코드라는 가정하에 이 메서드가 불려진 것이다.
		
		// resin specific 
		File smapFile = new File(workDirectory, ste.getClassName().replace(".", "/") + ".java.smap");
		if( !smapFile.exists() ) 
			// tomcat specific 
			smapFile = new File(workDirectory, ste.getClassName().replace(".", "/") + ".class.smap");
		
		if( !smapInfo.containsKey(ste.getClassName()) ) {
			Map<Integer, SmapFile> smapFiles = parseSmapFile(smapFile);
			smapInfo.put(ste.getClassName(), smapFiles);
		}
		
		Map<Integer, SmapFile> smapFiles = smapInfo.get(ste.getClassName());
		for(SmapFile smap : smapFiles.values()) {
			int line = smap.findInputLine(ste.getLineNumber());
			if( line!=-1 ) {
				return new StackTraceElement(ste.getClassName(), ste.getMethodName(),
					smap.getPath(), line);
			}
		}
		
		return ste;
	}

	private Map<Integer, SmapFile> parseSmapFile(File smapFile) throws IOException {
		Map<Integer, SmapFile> smapFiles = new HashMap<Integer, SmapFile>();
		
		int lastFileId = 0;
		Scanner scan = new Scanner(smapFile);
		try {
			int status = 0; // 1 - File, 2 - Lines, 3 - End.
			while( scan.hasNextLine() ) {
				String line = scan.nextLine();
				if( line.matches("\\*F.*") ) {
					status = 1;
					continue;
				}
				if( line.matches("\\*L.*") ) { 
					status = 2;
					continue;
				}
				if( line.matches("\\*E.*") ) {
					status = 3;
					break;
				}
				
				Matcher m = null;
				switch(status) {
				case 1:
					m = Pattern.compile("(\\+?)\\s*(\\d+)\\s+(.+)").matcher(line);
					m.find(); // 찾아져야만 한다. 스펙상 :)
					
					SmapFile sf = new SmapFile();
					sf.setId(Integer.parseInt(m.group(2)));
					sf.setName(m.group(3));
					if( m.group(1).equals("+") ) {
						String filePath = scan.nextLine();
						sf.setPath(filePath);
					}
					smapFiles.put(sf.getId(), sf);
					lastFileId = sf.getId();
					break;
				case 2:
					m = Pattern.compile("(\\d+)(#\\d+)?(,\\d+)?:(\\d+)(,\\d+)?").matcher(line);
					m.find();
					int jspStart = getInt(m.group(1));
					int fileId = getInt(m.group(2)==null ? String.valueOf(lastFileId) : m.group(2));
					int repeat = getInt(m.group(3));
					int javaStart = getInt(m.group(4));
					int javaInc = getInt(m.group(5));
					
					if( fileId==-1 ) 
						fileId = lastFileId;
					
					SmapLine sl = new SmapLine();
					sl.setInputStartLine(jspStart);
					sl.setFileId(fileId);
					sl.setRepeatCount(repeat);
					sl.setOutputStartLine(javaStart);
					sl.setOutputLineIncrement(javaInc);
	
					smapFiles.get(fileId).getLines().add(sl);
					break;
				}
			}
		} finally {
			scan.close();
		}
		return smapFiles;
	}
	
	private int getInt( String str ) {
		if( str==null )
			return 1;
		Matcher m = patternNumber.matcher(str);
		if( !m.find() )
			return -1;
		return Integer.parseInt(m.group());
	}
}
