package com.cashservice.service;

import com.cashservice.controller.request.RequestCashOperationDto;
import com.cashservice.controller.response.ResponseAccountDto;

public interface CashService {

    ResponseAccountDto deposit(RequestCashOperationDto dto);

    ResponseAccountDto withdraw(RequestCashOperationDto dto);
}
