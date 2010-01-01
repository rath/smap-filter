package rath.tools.web.smap;

import java.util.ArrayList;
import java.util.List;

public class SmapFile {
	private int id;
	private String name;
	private String path;
	private List<SmapLine> lines = new ArrayList<SmapLine>();;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public void setLines(List<SmapLine> lines) {
		this.lines = lines;
	}
	public List<SmapLine> getLines() {
		return lines;
	}
	
	public int findInputLine( int outputLine ) {
		for(SmapLine line : lines) {
			int no = line.getReverseLineNumber(outputLine);
			if( no!=-1 )
				return no;
		}
		return -1;
	}
	
	@Override
	public String toString() {
		return "SmapFile [id=" + id + ", name=" + name + ", path=" + path + "]";
	}
}
