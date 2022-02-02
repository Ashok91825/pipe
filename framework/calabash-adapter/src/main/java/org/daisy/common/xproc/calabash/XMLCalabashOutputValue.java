package org.daisy.common.xproc.calabash;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.model.Step;

import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import java.util.function.Consumer;

import org.daisy.common.saxon.SaxonOutputValue;
import org.daisy.common.transform.OutputValue;
import org.daisy.common.transform.TransformerException;

public class XMLCalabashOutputValue extends SaxonOutputValue {

	private final WritablePipe pipe;

	public XMLCalabashOutputValue(WritablePipe value, XProcRuntime runtime) {
		super(itemConsumer(value::write),
		      runtime.getProcessor().getUnderlyingConfiguration());
		this.pipe = value;
	}

	public XMLCalabashOutputValue(SaxonOutputValue value) {
		super(value);
		pipe = createWritablePipe(value.asXdmItemConsumer());
	}

	public static XMLCalabashOutputValue of(OutputValue<?> value) throws IllegalArgumentException {
		if (value instanceof XMLCalabashOutputValue)
			return (XMLCalabashOutputValue)value;
		else if (value instanceof SaxonOutputValue)
			return new XMLCalabashOutputValue((SaxonOutputValue)value);
		else
			throw new IllegalArgumentException("can not create XMLCalabashOutputValue from " + value);
	}

	public WritablePipe asWritablePipe() {
		return pipe;
	}

	/**
	 * <p>The returned {@link Consumer} will throw a {@link TransformerException} that wraps:</p>
	 * <ul>
	 *   <li>a {@link IllegalArgumentException} when an item is supplied that is not a node.</li>
	 * </ul>
	 */
	public Consumer<XdmItem> asXdmItemConsumer() {
		return super.asXdmItemConsumer();
	}

	private static WritablePipe createWritablePipe(Consumer<XdmItem> consumer) {
		return new WritablePipe() {
			private boolean written = false;
			public void canWriteSequence(boolean sequence) {
				throw new UnsupportedOperationException();
			}
			public boolean writeSequence() {
				return true;
			}
			public void write(XdmNode node) {
				consumer.accept(node);
				written = true;
			}
			public void setWriter(Step step) {}
			public void resetWriter() {
				if (written)
					throw new UnsupportedOperationException("Already written");
			}
			public void close() {}
		};
	}

	private static Consumer<XdmItem> itemConsumer(Consumer<XdmNode> nodeConsumer) {
		return item -> {
			if (item instanceof XdmNode)
				nodeConsumer.accept((XdmNode)item);
			else
				throw new TransformerException(new IllegalArgumentException("expected a node"));
		};
	}
}
