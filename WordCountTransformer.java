package streams;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;

public class WordCountTransformer implements Transformer<String, String, KeyValue<String, Long>> {

  private KeyValueStore<String, Long> kvStore;

  @Override
  @SuppressWarnings("unchecked")
  public void init(final ProcessorContext context) {
    context.schedule(Duration.ofMillis(5_000), PunctuationType.STREAM_TIME, timestamp -> {
      KeyValueIterator<String, Long> iter = kvStore.all();
      System.out.println("------ " + context.taskId() + " - " + timestamp + " -----" + " ");
      while (iter.hasNext()) {
        KeyValue<String, Long> entry = iter.next();
        System.out.println("[" + entry.key + ", " + entry.value + "]");
        //context.forward(entry.key, entry.value);
      }
    });
    this.kvStore = (KeyValueStore<String, Long>) context.getStateStore("Counts");
  }

  @Override
  public KeyValue<String, Long> transform(String word, String dummy) {
    Long oldValue = this.kvStore.get(word);
    Long newValue = (oldValue == null) ? 1L : oldValue + 1L;
    this.kvStore.put(word, newValue);
  
    return KeyValue.pair(word, newValue); // ✅ emit updated count
  }
  

  @Override
  public void close() {
  }
}
