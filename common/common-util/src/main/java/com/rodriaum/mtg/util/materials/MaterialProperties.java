package com.rodriaum.mtg.util.materials;

import com.rodriaum.mtg.util.MTGDirection;

public class MaterialProperties
{
	public static final MaterialProperty<Integer> AGE_0_25 = new MaterialProperty<>("age");
	public static final MaterialProperty<Integer> AGE_0_3 = new MaterialProperty<>("age");
	public static final MaterialProperty<Integer> PICKLES_1_4 = new MaterialProperty<>("pickles");

	public static final MaterialProperty<Boolean> SNOWY = new MaterialProperty<>("snowy");

	public static final MaterialProperty<MTGDirection> HORIZONTAL_DIRECTION = new MaterialProperty<>("horizontal_direction");

	public static final MaterialProperty<MTGDirection> DIRECTION = new MaterialProperty<>("direction");
}
