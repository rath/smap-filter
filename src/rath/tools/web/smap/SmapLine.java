package rath.tools.web.smap;

public class SmapLine {
	private int inputStartLine;
	private int fileId;
	private int repeatCount;
	private int outputStartLine;
	private int outputLineIncrement;
	
	public int getInputStartLine() {
		return inputStartLine;
	}
	public void setInputStartLine(int inputStartLine) {
		this.inputStartLine = inputStartLine;
	}
	public int getFileId() {
		return fileId;
	}
	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	public int getRepeatCount() {
		return repeatCount;
	}
	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}
	public int getOutputStartLine() {
		return outputStartLine;
	}
	public void setOutputStartLine(int outputStartLine) {
		this.outputStartLine = outputStartLine;
	}
	public int getOutputLineIncrement() {
		return outputLineIncrement;
	}
	public void setOutputLineIncrement(int outputLineIncrement) {
		this.outputLineIncrement = outputLineIncrement;
	}
	
	public int getReverseLineNumber( int outputLineNo ) {
		if( outputLineNo >= this.outputStartLine && 
			outputLineNo <= this.outputStartLine + (outputLineIncrement * repeatCount) ) 
			return inputStartLine + (outputLineNo - outputStartLine) / outputLineIncrement; 
		return -1;
	}
}
