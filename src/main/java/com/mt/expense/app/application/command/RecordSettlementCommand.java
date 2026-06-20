package com.mt.expense.app.application.command;

import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.Money;
import com.mt.expense.app.domain.vo.UserId;

public record RecordSettlementCommand(GroupId groupId, UserId from, UserId to, Money amount) {}
