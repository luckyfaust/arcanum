package app.arcanum.tasks;

public interface ITaskCallbackable {
	void ErrorOccurred(String message, Throwable ex);
	void PostExecuteCalled();
}
