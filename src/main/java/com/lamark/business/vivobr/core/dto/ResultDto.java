package com.lamark.business.vivobr.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class ResultDto {
	String word;
	Integer result;
}
