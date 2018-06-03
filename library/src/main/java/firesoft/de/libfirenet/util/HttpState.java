/*
 * Copyright (c) 2018.  David Schlossarczyk
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the full license visit https://www.gnu.org/licenses/gpl-3.0.
 */

package firesoft.de.libfirenet.util;

/**
 * Enum mit Stati welche der Loader als Feedback zurückgibt
 */
public enum HttpState {

    /**
     * Loader läuft nicht
     */
    NOT_RUNNING,

    /**
     * Loader initalisiert
     */
    INITALIZING,

    /**
     * Loader läuft
     */
    RUNNING,

    /**
     * Loader hat die Arbeit erfolgreich abgeschlossen
     */
    COMPLETED,

    /**
     * Loader hat die Arbeit nicht erforderlich abgeschlossen
     */
    FAILED
}
