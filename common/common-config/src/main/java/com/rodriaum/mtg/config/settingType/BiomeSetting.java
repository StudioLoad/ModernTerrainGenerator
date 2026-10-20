package com.rodriaum.mtg.config.settingType;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.IWorldConfig;

/**
 * Same as a Setting, except it also provides a world config when reading. Necessary to fetch the min and max height of a preset.
 */
public abstract class BiomeSetting<T> extends Setting<T>{
    protected BiomeSetting(String name) {
        super(name);
    }

    public abstract T read(String stringValue, IMaterialReader materialReader, IWorldConfig worldConfig) throws InvalidConfigException;
}
