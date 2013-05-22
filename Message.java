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

/**
 * This is a class of objects that contain the lines of
 * text that make up each individual e-mail message, as well
 * as information pertaining to its attachments.
 * 
 * @see EudoraAttach
 */
class Message {
  private String[] message;
  private String[] attachments;
  private String[] alreadyConvertedAttachments;
  private String xAttachments;
  
  /**
   * Constructor.
   * 
   * @param message an array of String containing the lines of
   * text of this message
   * @param xAttachments a String containing this message's X-Attachments
   * field
   */
  Message(String[] message, String xAttachments){
    this.message = message;
    this.xAttachments = xAttachments;
    abstractConvertedAttachments();
    abstractAttachments();
  }
  
  /**
   * Counts the number of semicolons in the X-Attachment field
   * (the number of semi-colons converts roughly to the number
   * of attachments).
   */ 
  private int countSemicolons(){
    String justSemicolons = xAttachments.replaceAll("[^;]", "");
    return justSemicolons.length();
  }
  
  /**
   * Removes an element from an array of String.
   * 
   * @param a array
   * @param element the int index of the element to be
   * removed from the array
   */
  public String[] removeElementFromArray(String[] a, int element){
    String[] a2 = new String[a.length - 1];
    for(int i = 0; i < element; i++)
      a2[i] = a[i];
    for(int i = element+1; i < a.length; i++)
      a2[i-1] = a[i];
    return a2;    
  }
  
  /**
   * Checks to see if this message already contains Attachment Converted
   * fields that match with the information contained in X-Attachment.
   */ 
  private int attachmentsMatch(){
    for(int i = 0; i < attachments.length; i++)
      for(int j = 0; j < alreadyConvertedAttachments.length; j++)
      if(alreadyConvertedAttachments[j].toLowerCase().equals(attachments[i].trim().toLowerCase()))
      return i;
    return -1;
  }
  
  /**
   * Abstracts from X-Attachments the path to each of this message's
   * attachments and records it in the String array "attachments".
   * If the information in "attachments" is a repeat from that in
   * the Attachment Converted fields, it deletes the respective
   * information from "attachments".
   */ 
  private String[] abstractAttachments(){
    int numOfAttachments = countSemicolons();
    
    if(numOfAttachments > 0){
      attachments = xAttachments.substring(14).split(";");
      if(alreadyConvertedAttachments != null)
        //remove all duplicate references to attachments
        while(attachmentsMatch() != -1)
        attachments = removeElementFromArray(attachments,attachmentsMatch());
      return attachments;
    }else{
      return (attachments = null);
    }
  }
  
  /**
   * Takes the information in the Attachment Converted fields and
   * stores it in an array.
   */ 
  private boolean abstractConvertedAttachments(){
    int j = 0;
    for(int i = 0; i < message.length; i++){
      if(message[i].indexOf("Attachment Converted: ") >= 0)
        j++;
    }
    
    alreadyConvertedAttachments = new String[j];
    j = 0;
    for(int i = 0; i < message.length; i++){
      if(message[i].indexOf("Attachment Converted: ") >= 0){
        alreadyConvertedAttachments[j] = message[i].substring(21).replaceAll("\"", "").trim();
        j++;
      }
    }
    return false;
  }
  
  /**
   * Returns all the paths to attachments contained in this message.
   * 
   * @return all the attachments' paths
   */ 
  String[] getAttachments(){
    return attachments;
  }
  
  /**
   * Returns the X-Attachments field.
   * 
   * @return the x-Attachments field.
   */
  String getXAttachments(){
    return xAttachments;
  }
  
  /**
   * Returns a String array in which each line represents each line of this
   * message, with the added Attachment Converted lines if necessary.
   * 
   * @return
   */ 
  String[] getMessage(){
    if(attachments != null){
      String[] newMessage = new String[message.length + attachments.length];
      //processes each line in the message
      for(int i = 0; i < message.length; i++)
        newMessage[i] = message[i];
      //adds the Attachment Converted lines
      for(int i = 0; i < attachments.length; i++){
        newMessage[i + message.length] = "Attachment Converted: \"" + attachments[i] + "\"";  
      }
      return newMessage;
    }
    return message;
  }
  
  /**
   * Returns a String representation of this message with the added
   * Attachment Converted lines if necessary.
   */ 
  public String toString(){
    String[] m = getMessage();
    String s = "";
    for(int i = 0; i < m.length; i++)
      s += m[i];
    return s;
  }
  
}