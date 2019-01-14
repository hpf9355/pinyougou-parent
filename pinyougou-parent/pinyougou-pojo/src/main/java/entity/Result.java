package entity;

/**
 * 品牌添加结果类
 * @author 胡鹏飞
 *
 */
public class Result {
	
	private boolean success;//添加是否成功
	private String message;//返回信息
	
	
	
	public Result(boolean success, String message) {
		super();
		this.success = success;
		this.message = message;
	}
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	
}
