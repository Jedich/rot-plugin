package tld.sofugames.rot;

import tld.sofugames.models.King;

@FunctionalInterface
public interface WarCondition {
	boolean apply(King atk, King def);
}
