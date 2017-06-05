package pl.edu.agh.toik.ec.visualization;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import pl.edu.agh.toik.ec.communication.Message;
import pl.edu.agh.toik.ec.visualization.conf.VisualizationConst;

/**
 * Created by baran on 09.05.17.
 */
public class VisualizationImpl implements Visualization {
	public static final Logger LOG = Logger.getLogger(VisualizationImpl.class);

	private VisualizationStrategy strategy;
	private VisualizationType type;

	private List<VisualizationMessage> cache = new ArrayList<>();

	@Autowired
	private SimpMessagingTemplate template;

	public VisualizationImpl() {
	}

	public VisualizationImpl(VisualizationStrategy strategy, VisualizationType type) {
		this.strategy = strategy;
		this.type = type;
		strategy.addObserver(this);
	}

	@Override
	public void notify(Message message) {
		strategy.addObserver(this);
		try {
			strategy.interpretMessage(VisualizationMessage.parse(message));
		} catch (VisualizationMessageParseException e) {
			LOG.error("Visualization received unparsable message of type " + message.getClass().getSimpleName());
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		cache.add((VisualizationMessage) arg);
		if (template == null) // TODO confirm possibility
			LOG.error("[VisualizationImpl.update] template is null!");
		else
			template.convertAndSend(VisualizationConst.WS_ENDPOINT_FITNESS, arg);
	}

	@Override
	public VisualizationType getType() {
		return type;
	}

	@Override
	public List<VisualizationMessage> getMessages() {
		return cache;
	}

	public void setStrategy(VisualizationStrategy strategy) {
		this.strategy = strategy;
		strategy.addObserver(this);
	}

	public void setType(VisualizationType type) {
		this.type = type;
	}
}
