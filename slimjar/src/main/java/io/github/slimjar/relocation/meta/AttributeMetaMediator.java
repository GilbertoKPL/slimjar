package io.github.slimjar.relocation.meta;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;

public final class AttributeMetaMediator implements MetaMediator {
    private final UserDefinedFileAttributeView view;

    public AttributeMetaMediator(final Path path) {
        this.view = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
    }

    @Override
    public String readAttribute(final String name) {
        try {
            final ByteBuffer buf = ByteBuffer.allocate(view.size(name));
            view.read(name, buf);
            buf.flip();
            return Charset.defaultCharset().decode(buf).toString();
        } catch (final Exception exception) {
            return null;
        }
    }

    @Override
    public void writeAttribute(final String name, final String value) {
        try {
            view.write(name, Charset.defaultCharset().encode(value));
        } catch (final Exception ignored) {
        }
    }
}
