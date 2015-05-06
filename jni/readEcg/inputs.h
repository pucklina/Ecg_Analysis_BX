/* file: inputs.h

This file defines the set of records that 'easytest' processes and that
'bxbep' uses for deriving performance statistics.
*/

// Uncomment only one of the next three lines.
//#define PHYSIOBANK	// 25 freely available MIT-BIH records from PhysioBank
#define MITDB	// 48 MIT-BIH records in 'mitdb'
// #define AHADB	// 69 AHA records in 'ahadb'

// If using MITDB or AHADB, edit the definition of ECG_DB_PATH to include
// the location where you have installed the database files.  Do not remove
// the initial "." or the space following it in the definition of ECG_DB_PATH!


#ifdef MITDB
#define ECG_DB_PATH	"mnt/sdcard/"
int Records[] = {100,101,102,103,104,105,106,107,108,109,111,112,
		 113,114,115,116,117,118,119,121,122,123,124,200,
		 201,202,203,205,207,208,209,210,212,213,214,215,
		 217,219,220,221,222,223,228,230,231,232,233,234};
#endif



#define REC_COUNT	(sizeof(Records)/sizeof(int))

