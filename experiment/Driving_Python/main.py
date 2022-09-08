#%%  
# Contains all actions that the Python component can execute on behalf of the Java component.
# author: Gilles Lijnzaad
import pylink as pl
import time
import client

# global variables, will be changed by input_handler
display_x = 0
display_y = 0
edf_file_name = ""
trial_number = 0
trial_id = ""

tracker = pl.EyeLink(None)
client.start_receiving_thread()

#%%
def prepare_experiment():

	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")

	tracker.openDataFile(edf_file_name)
	pl.flushGetkeyQueue()                               # cleanup
	tracker.setOfflineMode()                                # idle mode

	tracker.sendCommand("screen_pixel_coords =  0 0 %d %d" % (display_x, display_y))
	tracker.sendMessage("DISPLAY_COORDS  0 0 %d %d" % (display_x, display_y))
	tracker.setSimulationMode("YES")

	# Assumption for next lines: eye-tracker is version 6.10
	tracker.sendCommand("select_parser_configuration 0")
	tracker.sendCommand("file_event_filter = LEFT,RIGHT,FIXATION,SACCADE,BLINK,MESSAGE,BUTTON")
	tracker.sendCommand("file_sample_data  = LEFT,RIGHT,GAZE,AREA,GAZERES,STATUS,HTARGET")

	# Select what data is available over the link (for online data accessing)
	link_event_flags = 'LEFT,RIGHT,FIXATION,SACCADE,BLINK,BUTTON,FIXUPDATE,INPUT'
	link_sample_flags = 'LEFT,RIGHT,GAZE,GAZERES,AREA,HTARGET,STATUS,INPUT'
	tracker.sendCommand("link_event_filter = %s" % link_event_flags)
	tracker.sendCommand("link_sample_data = %s" % link_sample_flags)

	tracker.sendCommand("button_function 5 'accept_target_fixation'")

	tracker.sendCommand("pupil_size_diameter = YES")
	do_calibration()


def do_calibration():
	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")

	
	pl.openGraphics()
	pl.setCalibrationColors((0, 0, 0), (175, 175, 175))
	pl.setCalibrationSounds("", "", "")
	tracker.setCalibrationType('HV9')
	tracker.doTrackerSetup()
	pl.closeGraphics()
	pl.resetBackground()

	
	

def prepare_trial():

	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")

	record_message = "record_status_message 'Trial %d: %s'" % (trial_number, trial_id)
	tracker.sendCommand(record_message)
	tracker.sendMessage("TRIALID " + trial_id)
	print(record_message)

def drift_correction():

	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")
	
	pl.openGraphics()
	pl.setCalibrationColors((0, 0, 0), (175, 175, 175))
	pl.setDriftCorrectSounds("", "off", "off")
	drift_result = tracker.doDriftCorrect(int(display_x/2), int(display_y/2), 1, 1)
	if drift_result == 27:
		drift_correction()
	pl.closeGraphics()

def repeat_correction():
	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")
	drift_result = tracker.doDriftCorrect(int(display_x/2), int(display_y/2), 1, 1)
	return drift_result

def start_recording():

	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")
                                            
	tracker.setOfflineMode()
	recording_error = tracker.startRecording(1, 1, 1, 1)
	print("started recording")
	if recording_error:                             # 0 if successful, error code otherwise
		report_error("TRIAL ERROR")

	pl.beginRealTimeMode(100)                   # tells Windows to give priority to this

	if not tracker.waitForBlockStart(1000, 1, 0):
		report_error("TRIAL ERROR")


def send_SYNCTIME(start_time):

	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")

	current_time = int(time.time() * 1000)
	sync_time = current_time - start_time
	tracker.sendMessage(str(sync_time) + " SYNCTIME")


def send_tracker(message):

	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")

	tracker.sendMessage(message)


def end_trial():
	
	pl.endRealTimeMode()
	pl.pumpDelay(100)
	tracker.stopRecording()
	while tracker.getkey():
		pass


def end_experiment():

	if tracker is not None:
		tracker.setOfflineMode()
		pl.msecDelay(500)

	tracker.closeDataFile()
	tracker.receiveDataFile(edf_file_name, edf_file_name)
	tracker.close()


def report_error(error_message):
	client.send(error_message)

# %%
