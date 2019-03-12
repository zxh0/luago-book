--[[
public void lock2seconds(Lock lock) {
	if (!lock.tryLock()) {
		throw new RuntimeException("Unable to acquire the lock!");
	}
	try {
		Thread.sleep(2000);
	} catch (InterruptedException e) {
		// ignore
	} finally {
		lock.unlock();
	}
}
]]

function lock2seconds(lock)
  if not lock:tryLock() then
    error("Unable to acquire the lock!")
  end
  pcall(function()
    sleep(2000)
  end)
  lock:unlock()
end

function lock2seconds(lock)
  if not lock:tryLock() then
    error({err = "Unable to acquire the lock!"})
  end
end

function lock2seconds(lock)
  if not lock:tryLock() then
    error {err = "Unable to acquire the lock!"}
  end
end

function lock2seconds(lock)
  lock:lock()
  pcall(sleep, 2000)
  lock:unlock()
end

function lock2seconds(lock)
  lock:lock()
  local ok, msg = pcall(sleep, 2000)
  lock:unlock()
  if ok then
    print("ok")
  else
    print("error: " .. msg)
  end
end
