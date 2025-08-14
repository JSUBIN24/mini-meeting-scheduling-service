package com.org.mini_doodle.dto.response;


import com.org.mini_doodle.domain.TimeInterval;

import java.util.List;

public record FreeBusyResponse(List<TimeInterval> busy, List<TimeInterval> free) {
}
