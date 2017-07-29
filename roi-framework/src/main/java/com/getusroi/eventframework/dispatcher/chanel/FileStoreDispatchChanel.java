package com.getusroi.eventframework.dispatcher.chanel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getusroi.config.RequestContext;

/**
 * Dispatcher Chanel Implementation for the File Store.<br>
 * Dispatches all the msessages to file configured in the EventFramework xml
 * 
 * @author Bizruntime
 *
 */
public class FileStoreDispatchChanel extends AbstractDispatchChanel {
	final static Logger logger = LoggerFactory.getLogger(FileStoreDispatchChanel.class);
	private String filePath;// ="D:/Work/ClientSpace/Getusroi/LogDispatcher";
	private String fileName;// ="LogDispatchChanel.txt";

	private File file;
	private FileWriter fw;
	private BufferedWriter bw;

	public FileStoreDispatchChanel(String chaneljsonconfig) throws DispatchChanelInitializationException  {
		this.chaneljsonconfig = chaneljsonconfig;
		initializeFromConfig();
	}

	public FileStoreDispatchChanel() {
	}

	/**
	 * This method is used to dispatch message to file
	 * 
	 * @param msg
	 *           : Object
	 */
	@Override
	public void dispatchMsg(Serializable msg,RequestContext requestContext,String eventId)throws MessageDispatchingException {
		logger.debug("inside dispatchMsg in FileStoreDispatcherChanel");
		try {
			// logger.info("dispatchmsg("+msg+");");
			this.bw.write(msg.toString());
			bw.flush();
			logger.debug("dispatchmsg(" + msg + "); written");
			try {
				closeResources();
			} catch (Throwable e) {
				throw new MessageDispatchingException("Error in closing the file resource :"+filePath+", with file name as : "+fileName);
			}
		} catch (IOException ioexp) {
			throw new MessageDispatchingException("FileStoreDispatchChanel failed to Dispatch EventMsg to file{"+filePath+"//"+fileName+"}",ioexp);
		}
	}

	/**
	 * This method is to initialize file location and file name
	 * 
	 * @param chaneljsonconfig
	 */
	// #TODO Write clean and better code for Chanel.
	public void initializeFromConfig()throws DispatchChanelInitializationException  {
		try {
			parseConfiguration(this.chaneljsonconfig);
			if (filePath != null && !filePath.isEmpty() && fileName != null && !fileName.isEmpty()) {
				logger.debug("file path in file dispatcher: " + filePath + "/" + fileName);
				file = new File(filePath + "/" + fileName);
				if (!file.exists())
					file.createNewFile();

				this.fw = new FileWriter(file.getAbsoluteFile(), true);
				this.bw = new BufferedWriter(fw);
			} else {
				logger.debug("initializeFromConfig filePath null or filename null");
			}
		
		} catch (ParseException | IOException e) {
			throw new DispatchChanelInitializationException("Failed to Initialize FileStoreDispatchChanel for config="+chaneljsonconfig,e); 
		}
	}// end of method

	/**
	 * This method is to parse json configuration
	 * 
	 * @param chaneljsonconfig
	 * @throws ParseException 
	 */
	private void parseConfiguration(String chaneljsonconfig) throws ParseException {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(chaneljsonconfig);
		JSONObject jsonObject = (JSONObject) obj;
		this.filePath = (String) jsonObject.get("filepath");
		this.fileName = (String) jsonObject.get("filename");
	}

		private void closeResources() throws Throwable{
		logger.debug("closing buffer writer and file writer");
		try {
			if(bw !=null)
			bw.close();
			if(fw !=null)
			fw.close();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}
	
	protected void finalize() throws Throwable {
		closeResources();
	}
}//end of class
