package com.transferservice.service;

import com.transferservice.controller.request.RequestTransferDto;
import com.transferservice.controller.response.ResponseAccountDto;

import java.util.UUID;

public interface TransferService {

    ResponseAccountDto transfer(UUID fromUserId, RequestTransferDto dto);
}
