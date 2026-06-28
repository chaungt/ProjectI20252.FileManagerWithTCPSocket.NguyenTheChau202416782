package protocol;
public interface Protocol {
	byte REGISTER = 1;
	byte LOGIN = 2;
	byte UPLOAD = 3;
	byte DOWNLOAD = 4;
	byte LIST = 5;
	byte DELETE = 6;
	byte SHARE = 7;
	byte OK = 10;
	byte ERROR = 11;
	byte FAILED = 12;
	byte ERROR_INVALID_FILE=20;
	byte ERROR_NOT_FOUND=21;
	byte ERROR_NO_PERMISSION=22;
	byte ERROR_SESSION_INVALID=23;
	byte ERROR_UNKNOWN=24;
}