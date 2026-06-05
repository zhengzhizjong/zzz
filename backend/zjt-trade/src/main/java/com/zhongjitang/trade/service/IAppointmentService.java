package com.zhongjitang.trade.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.AppointmentCancelRequest;
import com.zhongjitang.trade.domain.dto.AppointmentCreateRequest;
import com.zhongjitang.trade.domain.dto.AppointmentModifyRequest;
import com.zhongjitang.trade.domain.entity.TradeAppointmentDO;
import com.zhongjitang.trade.domain.vo.AppointmentResponse;
import com.zhongjitang.trade.domain.vo.MyAppointmentVO;

public interface IAppointmentService {

    R<AppointmentResponse> create(AppointmentCreateRequest request);

    R<PageResult<TradeAppointmentDO>> page(Integer page, Integer pageSize, Long storeId,
                                            Long memberId, Integer status);

    R<TradeAppointmentDO> getById(Long id);

    R<MyAppointmentVO> getMyAppointments(Long memberId, String statusGroup);

    R<AppointmentResponse> modify(Long id, AppointmentModifyRequest request);

    R<Void> cancel(Long id, AppointmentCancelRequest request);
}
