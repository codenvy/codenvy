/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.im.commands;

import com.codenvy.im.utils.TarUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * This command unpacks archives of "tar.gz" type into the directory to uncompress.
 * If directory to uncompress is non-existed, it will be created.
 *
 * @author Dmytro Nochevnov
 */
public class UnpackCommand implements Command {
    private final String description;
    private final Path   pack;
    private final Path   dirToUnpack;

    private static final Logger LOG = Logger.getLogger(UnpackCommand.class.getSimpleName());

    public UnpackCommand(Path pack, Path dirToUnpack, String description) {
        this.pack = pack;
        this.dirToUnpack = dirToUnpack;
        this.description = description;
    }

    /** {@inheritDoc} */
    @Override
    public String execute() throws CommandException {
        LOG.log(Level.INFO, toString());
        try {
            TarUtils.uncompress(pack, dirToUnpack);
        } catch (IOException e) {
            throw new CommandException(e.getMessage(), e);
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return format("Unpack '%s' into '%s'", pack.toString(), dirToUnpack.toString());
    }
}
