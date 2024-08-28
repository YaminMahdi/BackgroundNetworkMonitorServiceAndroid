## Aalways Running Background Network Monitor Service - Android

This is an example of always running Service in Android

### Explanation

#### Foreground Service:
`NetworkObserverService` is set up as a foreground service with a notification, which is necessary for long-running tasks to ensure it’s not killed by the system.
`START_STICKY` in `onStartCommand()` ensures that the service will be restarted if it gets killed.

#### Broadcast Receiver:
`WorkRestartReceiver` is used to handle broadcasts that will trigger the `ServiceRestartWorker` to check and restart the service.

#### WorkManager:
`ServiceRestartWorker` is scheduled to run periodically to check if `NetworkObserverService` is running. If it's not running, it starts the service.
The periodic work is scheduled to run every 16 minutes, which is a reasonable frequency considering WorkManager’s minimum interval constraints.

#### Handling Service Restart:
By sending a broadcast to `WorkRestartReceiver` in the `onDestroy()` method of `NetworkObserverService`, the service is requested to restart. `WorkRestartReceiver` then schedules a one-time work request to ensure the service restarts.
`ServiceRestartWorker` will periodically check if `NetworkObserverService` is running and start it if necessary, providing an additional layer of reliability.


### Considerations

#### Battery Optimization:

Frequent restarts and periodic checks can impact battery life. Ensure that your use case justifies this approach.

#### Service Lifetime Management:
The combination of WorkManager and foreground service helps maintain the service's operation but can be subject to system constraints and optimizations.

#### Permissions:
Ensure that the necessary permissions and settings (such as auto-start permissions) are properly handled and requested from the user if needed.
