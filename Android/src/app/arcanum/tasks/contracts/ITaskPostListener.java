package app.arcanum.tasks.contracts;

public interface ITaskPostListener {
	void onPostExecute(String taskname, Object input, Object result);
}
