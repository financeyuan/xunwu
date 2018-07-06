package com.yaoyao.online.base;

import lombok.Data;

/**
 * Title: ApiResponse
 * 
 * @author yuanpb
 * @date 2018年5月31日 API格式封装
 */
@Data
public class ApiResponse {

	private Integer code;

	private String message;

	private Object data;

	private boolean more;

	public ApiResponse(Integer code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public ApiResponse() {
		this.code = Status.SUCCESS.getCode();
		this.message = Status.SUCCESS.getStandardMessage();

	}

	public static ApiResponse ofMessage(Integer code, String message) {
		return new ApiResponse(code, message, null);
	}

	public static ApiResponse ofSuccess(Object data) {
		return new ApiResponse(Status.SUCCESS.getCode(), Status.SUCCESS.getStandardMessage(), data);
	}

	public static ApiResponse ofStatus(Status status) {
		return new ApiResponse(status.getCode(), status.getStandardMessage(), null);
	}

	public enum Status {

		SUCCESS(200, "OK"),

		BAD_REQUEST(400, "BAD Request"),

		INTERNAL_SERVER_ERROR(500, "Unkonw Internal Error"),

        NOT_FOUND(404, "Not Found"),

		NOT_VAILD_PARAMS(400001, "Not Vaild Params"),

		NOT_SUPPORTED_OPERATION(400002, "Not Operation Supported"),

		NOT_LOGIN(400003, "Not Login");

		private Integer code;

		private String standardMessage;

		Status(Integer code, String standardMessage) {
			this.code = code;

			this.standardMessage = standardMessage;
		}

		public Integer getCode() {
			return code;
		}

		public void setCode(Integer code) {
			this.code = code;
		}

		public String getStandardMessage() {
			return standardMessage;
		}

		public void setStandardMessage(String standardMessage) {
			this.standardMessage = standardMessage;
		}

	}
}
