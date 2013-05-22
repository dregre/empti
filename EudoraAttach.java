//   Eudora Mailboxes Preparer for Thunderbird's Importer
//                 EMPTI v. 1.2
//                 
//                 by André Gregori
//
//
//   The purpose of this small program is to help  users
//   who are switching from Eudora to Thunderbird  e-mail
//   client, but who would still like to keep  their data
//   intact. Ultimately, one of the biggest  challenges in
//   switching from Eudora is to keep the email messages
//   linked to their respective  attachments. As Eudora
//   keeps a separate attachment  folder of its own, it
//   does not, unlike Thunderbird,  keep a copy of every
//   attachment received and sent  within the data of the
//   individual messages.
//
//   The Thunderbird importer seems to handle files 
//   attached to received messages quite well; it does 
//   not, however, import the attachments of sent  messages
//   (messages in your Out box).
//
//   To solve this, this little program was written. 
//   Basically what it does is it looks at each message  in
//   your mailboxes and tries to see if it has fields 
//   which begins with the words, "Attachment Converted." 
//   These are the fields that the Thunderbird importer
//   looks for when it is trying to find attachments.  By
//   default, Eudora does not add these fields to sent 
//   messages; rather, it keeps track of attachments by
//   listing them under the "X-Attachments" field. What 
//   this program does is it opens each of Eudora's .mbx 
//   files, takes the information contained in each
//   message's "X-Attachments" field and replicates it  by
//   adding new "Attachment Converted" fields.  This
//   program outputs new .mbx files that can be used  to
//   replace the original .mbx files in the Eudora  folder,
//   so that when you launch the Thunderbird  import
//   utility it will also copy attachments linked to sent
//   messages.
//
//   Please be advised that if you launch Eudora after
//   you've replaced the .mbx files it might want to
//   reindex your mailboxes, which would result in the loss
//   of a variety of useful data (such as messages'
//   read/unread status, labels, etc). Therefore, it  is
//   highly recommended that you BACKUP your Eudora
//   completely before you replace your original .mbx
//   files.
//
//   This program might work with the import utilities of 
//   other e-mail clients, but this hasn't been tested.  If
//   you end up testing it with other email clients, 
//   please let me know how it goes by writing to me at
//   http://www.andregregori.com/contact.php
//
//   Feel free to redistribute this program, so long as 
//   you include the fact that I wrote it and make the code
//   available with each redistribution. Feel free to make 
//   changes to the code, although these changes must be 
//   documented if they are to be redistributed with this 
//   program. Sale of this program, or the bundling of it 
//   with any other program bound for sale, is forbidden.  
//   Keep it open source, keep it free. 

import java.io.*;
import java.util.*;

/**
 * This is a fixer for Eudora's .mbx files, which adds to
 * each message the "Attachment Converted" fields that the
 * Thunderbird import utility uses to locate attachments. All
 * the reading and writing of .mbx files is done through here.
 */

public class EudoraAttach {
  private ArrayList<Message> collection = new ArrayList<Message>();
  
  /**
   * This is the main method. It reads all the .mbx files in
   * a given folder and processes them with processFile().
   * <br><br>
   * It takes two arguments only:
   * <br><br>
   * 1. String representation of the path to the Eudora directory
   * or to the directory containing the original .mbx files.
   * <br>
   * 2. String path to different directory where new .mbx files
   * will be stored.
   */
  public static void main(String args[]){
    
    System.out.println("");
    System.out.println("=====EMPTI V.1.2 by Andre Gregori=====");
    
    // Terminates if user inputs wrong number of arguments
    if(args.length != 2){
      System.out.println("");
      System.out.println("Syntax failure: please write commands in this form:");
      System.out.println("     [Origin Directory's Path] [Destination Directory's Path]");
      System.out.println("");
      System.out.println("For example:");
      if(System.getProperty("os.name").equals("Mac OS X"))
        System.out.println("     \"/Users/JohnSmith/Documents/Eudora Folder/Mail Folder\" \"/Users/JohnSmith/Documents/Eudora Fixed Mailboxes\"");
      else
        System.out.println("     \"C:\\Program Files\\Eudora\" \"C:\\Eudora_Backup\"");
      System.out.println("");
      System.out.println("Please note:");
      System.out.println("The origin directory cannot contain the destination directory.");
      System.out.println("");
      System.exit(0);
    }
    
    File path1 = new File(args[0]);
    File path2 = new File(args[1]);
    
    // Terminates if user does not input paths that point to directories
    if(!(path1.isDirectory() && path2.isDirectory())){
      System.out.println("Failure: paths have to point to directories.");
      System.exit(0);
    }
    
    // Terminates if user inputs wrong number of arguments
    if(path1.getPath().equals(path2.getPath())){
      System.out.println("Failure: origin directory has to be different from destination directory.");
      System.exit(0);
    }
    
    
    // Terminates if user inputs an origin directory that contains the destination directory 
    if(path2.getPath().indexOf(path1.getPath()) >= 0){
      System.out.println("Failure: origin directory cannot contain destination directory.");
      System.exit(0);
    }
    
    
    
    // Runs the program
    run(path1, path2, false);
  }
  
  /**
   * Recursively runs processDir() on all subdirectories.
   */
  private static void run(File path1, File path2, boolean isSubdir){
    // This filter only returns directories  
    FileFilter fF = new FileFilter() {
      public boolean accept(File file) {
        if(file.isDirectory())
          return true;
        return false;
      } 
    };
    
    // Deletes empty subdirectories
    if(!processDir(path1, path2) && isSubdir)
      path2.delete();
    
    for (File f : path1.listFiles(fF)) {
      File f2 = new File(path2.getPath() + File.separatorChar + f.getName());
      f2.mkdirs();
      run(f, f2, true);
    }
  }
  
  /**
   *    Processes each mailbox file in path1 adding the appropriate "Attachment Converted"
   *    fields, and saves output in a homonymous mailbox file located in path2.
   */
  private static boolean processDir(File path1, File path2){
    // This filter only returns mailbox files
    FileFilter fF = new FileFilter() {
      public boolean accept(File file) {
        
        try{
          // Checks for matching file type code in OSX.
          // 1413830740 is the OSX file type code for Eudora mailboxes. 
          if(System.getProperty("os.name").equals("Mac OS X") && com.apple.eio.FileManager.getFileType(file.getPath()) == 1413830740)
            return true;
          else{
            //  Checks for file extension in Windows
            return (file.getName().length() > 4 && file.getName().substring(file.getName().length() - 4).toLowerCase().equals(".mbx"));
          }
        } catch (IOException e) { 
          // catch possible io errors from readLine()
          System.out.println("Uh oh, got an IOException error!");
          e.printStackTrace();
        }
        return false;
      }
    };
    
    // Process each mailbox file in path1's directory
    File [] mailboxes = path1.listFiles(fF);
    
    // Terminates (fails) if there are no mailbox files
    if(mailboxes.length <= 0)
      return false;
    
    // Processes each mailbox file.
    for (File f : mailboxes) {
      System.out.println("Processing file \"" + f.getName() + "\" ...");
      EudoraAttach ea = new EudoraAttach();
      ea.processFile(f.getPath(), path2.getPath() + File.separator + f.getName());
      System.out.println("Done!");
    }
    
    return true;
    
  }
  
  /**
   * Checks to see if text marks the start of a new message.
   * 
   * @param text Line from .mbx file.
   */ 
  private boolean isMessageStart(String text){
    if(text.startsWith("From - ") || text.startsWith("From ???@??? "))
      return true;
    return false;
  }
  
  /**
   * Checks to see if line of text is an X-Attachment field.
   *
   * @param text Line from .mbx file.
   */ 
  private boolean isXAttachment(String text){
    if(text.startsWith("X-Attachments: "))
      return true;
    return false;
  }
  
  /**
   * Creates Message object.
   * 
   * @param message ArrayList containing all the lines of text
   * in a given message.
   */ 
  public Message generateMessage(ArrayList<String> message){
    String[] messageBody = new String[message.size()];
    String xAttachments = "";
    
    for (int i = 0; i < message.size(); i++) {
      messageBody[i] = message.get(i);
      if(isXAttachment(messageBody[i]))
        xAttachments = messageBody[i];
    }
    
    message.clear();
    
    return new Message(messageBody, xAttachments);
  }
  
  /**
   * Reads a .mbx file, processes it, abstracts the information
   * from the X-Attachment fields, creates new Attachment Converted
   * fields containing the information, and saves the new .mbx
   * files in a given location.
   *
   * @param readPath the path to the file to be read
   * @param writePath the path to the file to be written
   */ 
  private void processFile(String readPath, String writePath) { 
    String record = null;
    
    try { 
      
      FileReader fr     = new FileReader(readPath);
      BufferedReader br = new BufferedReader(fr);
      
      FileWriter fw  = new FileWriter(writePath);
      PrintWriter pw = new PrintWriter(fw, true);
      
      record = new String();
      ArrayList<String> message = new ArrayList<String>();
      Message m = null;
      
      while ((record = br.readLine()) != null) {
        if(isMessageStart(record)){
          //collection.add(generateMessage(message));
          m = generateMessage(message);
          writeMessage(m, pw);
        }
        message.add(record);
        //System.out.println(record);
      }
      m = generateMessage(message);
      writeMessage(m, pw);
      //collection.add(generateMessage(message));
      
      pw.close();
      
    } catch (IOException e) { 
      // catch possible io errors from readLine()
      System.out.println("Uh oh, got an IOException error!");
      e.printStackTrace();
    }
  } // end of readMyFile()
  
  /**
   * Writes the message in the file provided in the PrintWriter.
   * 
   * @param m the message
   * @param pw the PrintWriter
   */ 
  private Message writeMessage(Message m, PrintWriter pw){
    String[] message = m.getMessage();
    for(int i = 0; i < message.length; i++){
      pw.print(message[i]);
      pw.print('\r');
      if(System.getProperty("os.name").indexOf("Windows") >= 0)
        pw.print('\n');
      pw.flush();
    }
    return m;
  }
  
}