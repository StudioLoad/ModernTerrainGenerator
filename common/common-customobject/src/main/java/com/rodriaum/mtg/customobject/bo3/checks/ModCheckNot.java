package com.rodriaum.mtg.customobject.bo3.checks;

import com.rodriaum.mtg.interfaces.IModLoadedChecker;

public class ModCheckNot extends ModCheck
{
	@Override
	public String makeString()
	{
		return makeString("ModCheckNot");
	}
	
	public boolean evaluate(IModLoadedChecker modLoadedChecker)
	{
		return !super.evaluate(modLoadedChecker);
	}
}
