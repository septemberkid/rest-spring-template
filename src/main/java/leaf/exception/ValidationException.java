package leaf.exception;

public class ValidationException extends RuntimeException {
    private static final String TAG = ValidationException.class.getName();
    private int code;
    public Throwable error;

    public static int HTTP_STATUS_CODE = 400;

    public ValidationException(){
        super();
    }

    public ValidationException(Throwable e){
        super(e.getMessage());
        this.code = ValidationException.HTTP_STATUS_CODE;
        this.error = e;
    }

    public ValidationException(Throwable e, String message){
        super(message);
        this.code = ValidationException.HTTP_STATUS_CODE;
        this.error = e;
    }

    public ValidationException(String message){
        super(message);
        this.code = ValidationException.HTTP_STATUS_CODE;
    }

    public ValidationException(String message, int lineOfCode){
        super(message+" ["+lineOfCode+"]");
        this.code = ValidationException.HTTP_STATUS_CODE;
    }

    public int getCode(){
        return this.code;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}