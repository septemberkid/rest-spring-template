package leaf.exception;

public class LeafException extends RuntimeException {
    private static final String TAG = LeafException.class.getName();
    private int code;
    public Throwable error;

    public static int HTTP_STATUS_CODE = 400;

    public LeafException(){
        super();
    }

    public LeafException(Throwable e){
        super(e.getMessage());
        this.code = LeafException.HTTP_STATUS_CODE;
        this.error = e;
    }

    public LeafException(Throwable e, String message){
        super(message);
        this.code = LeafException.HTTP_STATUS_CODE;
        this.error = e;
    }

    public LeafException(String message){
        super(message);
        this.code = LeafException.HTTP_STATUS_CODE;
    }

    public LeafException(String message, int statusCode){
        super(message);
        this.code = statusCode;
    }

    public int getCode(){
        return this.code;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}