package com.yanzi.common.constants;

public enum SuccessCode {
	UNKNOWN_ERROR(-1), SUCCESS(0), PARAMS_IS_NULL(1);

	private int code;

	private SuccessCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static SuccessCode getByName(String name) {
		try {
			return SuccessCode.valueOf(name);
		} catch (Exception e) {
			return UNKNOWN_ERROR;
		}
	}
}
