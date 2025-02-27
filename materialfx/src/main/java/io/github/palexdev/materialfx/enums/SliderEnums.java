/*
 * Copyright (C) 2022 Parisi Alessandro
 * This file is part of MaterialFX (https://github.com/palexdev/MaterialFX).
 *
 * MaterialFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MaterialFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MaterialFX.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.palexdev.materialfx.enums;

import io.github.palexdev.materialfx.controls.MFXSlider;

/**
 * Class that contains some enumerators to be used with {@link MFXSlider}.
 */
public class SliderEnums {

	private SliderEnums() {}

	/**
	 * Enumeration to specify the snap behavior of {@link MFXSlider}.
	 */
	public enum SliderMode {
		DEFAULT, SNAP_TO_TICKS
	}

	/**
	 * Enumeration to specify on which side to show the {@link MFXSlider}'s popup.
	 */
	public enum SliderPopupSide {
		DEFAULT, OTHER_SIDE
	}
}
