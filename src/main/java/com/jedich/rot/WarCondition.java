package com.jedich.rot;

import com.jedich.models.King;

@FunctionalInterface
public interface WarCondition {
	boolean apply(King atk, King def);
}
