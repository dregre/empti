EMPTI
=====

Eudora Mailboxes Preparer for Thunderbird's Importer

This little Java program prepares your Eudora mailbox files such that sent attachments are importable by Thunderbird. What the program does is it extracts the path associated with each sent attachment (stored originally in the "X-Attachments" header field), and copies it to the bottom of the respective message after the words "Attachment Converted." This new field allows Thunderbird's Eudora importer to "realize" that sent attachments actually exist, which in turn allows it to look for and effectively import those attachments.

Be warned that any importer will only effectively be able to import the files that it can find. Thus if there is any discrepancy between the paths stored in your messages and the actual location of those files, the importer will not be able to locate and import that file.

Get binaries at:
http://sourceforge.net/projects/empti/
