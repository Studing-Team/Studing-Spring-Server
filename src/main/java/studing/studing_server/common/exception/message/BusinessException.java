package studing.studing_server.common.exception.message;

public class BusinessException extends RuntimeException {
    private ErrorMessage errorMessage;

    public BusinessException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }


    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

}
