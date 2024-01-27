package com.crashinvaders.common;

import com.badlogic.gdx.InputProcessor;

import java.util.Comparator;

/** Same old InputMultiplexer but it orders the processors by their order value. */
public class OrderedInputMultiplexer implements InputProcessor {

    private final Comparator<Wrapper> comparator;
    private final ValueArrayMap<InputProcessor, Wrapper> processors = new ValueArrayMap<>(4);

    private int maxPointers = 1; // Single touch by default

    public OrderedInputMultiplexer() {
        comparator = new WrapperComparator();
    }

    public int getMaxPointers() {
        return maxPointers;
    }

    public void setMaxPointers(int maxPointers) {
        this.maxPointers = maxPointers;
    }

    public void addProcessor(InputProcessor processor) {
        addProcessor(processor, 0);
    }

    public void addProcessor(InputProcessor processor, int order) {
		if (processor == null) throw new NullPointerException("Processor cannot be null");
		processors.put(processor, new Wrapper(processor, order));
        processors.sort(comparator);
	}

	public void removeProcessor(InputProcessor processor) {
        processors.remove(processor);
	}

	/** @return the number of processors in this multiplexer */
	public int size () {
		return processors.size();
	}

	public void clear () {
		processors.clear();
	}

    @Override
    public boolean keyDown(int keycode) {
        for (int i = 0, n = processors.size(); i < n; i++)
            if (processors.getValueAt(i).keyDown(keycode)) return true;
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        for (int i = 0, n = processors.size(); i < n; i++)
            if (processors.getValueAt(i).keyUp(keycode)) return true;
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        for (int i = 0, n = processors.size(); i < n; i++)
            if (processors.getValueAt(i).keyTyped(character)) return true;
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer >= maxPointers) return false;

        for (int i = 0, n = processors.size(); i < n; i++)
            if (processors.getValueAt(i).touchDown(screenX, screenY, pointer, button)) return true;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer >= maxPointers) return false;

        for (int i = 0, n = processors.size(); i < n; i++)
            if (processors.getValueAt(i).touchUp(screenX, screenY, pointer, button)) return true;
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        if (pointer >= maxPointers) return false;

        for (int i = 0, n = processors.size(); i < n; i++)
            if (processors.getValueAt(i).touchCancelled(screenX, screenY, pointer, button)) return true;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer >= maxPointers) return false;

        for (int i = 0, n = processors.size(); i < n; i++)
            if (processors.getValueAt(i).touchDragged(screenX, screenY, pointer)) return true;
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        for (int i = 0, n = processors.size(); i < n; i++)
            if (processors.getValueAt(i).mouseMoved(screenX, screenY)) return true;
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        for (int i = 0, n = processors.size(); i < n; i++)
            if (processors.getValueAt(i).scrolled(amountX, amountY)) return true;
        return false;
    }

    private static class Wrapper implements InputProcessor {
        private final InputProcessor processor;
        private final int order;

        public Wrapper(InputProcessor processor, int order) {
            this.processor = processor;
            this.order = order;
        }
        @Override
        public boolean keyDown(int keycode) {
            return processor.keyDown(keycode);
        }
        @Override
        public boolean keyUp(int keycode) {
            return processor.keyUp(keycode);
        }
        @Override
        public boolean keyTyped(char character) {
            return processor.keyTyped(character);
        }
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return processor.touchDown(screenX, screenY, pointer, button);
        }
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return processor.touchUp(screenX, screenY, pointer, button);
        }
        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
            return processor.touchCancelled(screenX, screenY, pointer, button);
        }
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return processor.touchDragged(screenX, screenY, pointer);
        }
        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return processor.mouseMoved(screenX, screenY);
        }
        @Override
        public boolean scrolled(float amountX, float amountY) {
            return processor.scrolled(amountX, amountY);
        }

        @Override
        public String toString() {
            if (processor != null) {
                return processor.toString();
            }
            return super.toString();
        }
    }

    private static class WrapperComparator implements Comparator<Wrapper> {
        @Override
        public int compare(Wrapper l, Wrapper r) {
            return CommonUtils.compare(l.order, r.order);
        }
    }
}
