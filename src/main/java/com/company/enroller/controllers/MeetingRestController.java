package com.company.enroller.controllers;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.data.domain.Sort;

import com.company.enroller.model.Meeting;
import com.company.enroller.model.Participant;
import com.company.enroller.persistence.MeetingService;
import com.company.enroller.persistence.ParticipantService;

@RestController
@RequestMapping("/meetings")
public class MeetingRestController {

	@Autowired
	MeetingService meetingService;

	@Autowired
	ParticipantService participantService;

// BASIC	

	@RequestMapping(value = "", method = RequestMethod.GET)
	// GET http://localhost:8080/meetings
	public ResponseEntity<?> getMeetings() {
		Collection<Meeting> meetings = meetingService.getAll();
		return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
	}

	// GET http://localhost:8080/meetings/2
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getMeeting(@PathVariable("id") long id) {
		Meeting meeting = meetingService.findByID(id);
		if (meeting == null) {
			return new ResponseEntity<>("Meeting does not exist", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
	}

	// POST http://localhost:8080/meetings
	@RequestMapping(value = "", method = RequestMethod.POST)
	public ResponseEntity<?> registerMeeting(@RequestBody Meeting meeting) {
		if (meetingService.findByID(meeting.getId()) != null) {
			return new ResponseEntity<>("Unable to create. A meeting with id " + meeting.getId() + " already exist.",
					HttpStatus.CONFLICT);
		}
		meetingService.add(meeting);
		return new ResponseEntity<Meeting>(meeting, HttpStatus.CREATED);
	}

// ADVANCED	

	// GET http://localhost:8080/meetings/2/participants
	@RequestMapping(value = "/{id}/participants", method = RequestMethod.GET)
	public ResponseEntity<?> getMeetingParticipants(@PathVariable("id") long id) {
		Meeting meeting = meetingService.findByID(id);
		if (meeting == null) {
			return new ResponseEntity<>("Meeting does not exist", HttpStatus.NOT_FOUND);
		}
		if (meeting.getParticipants().isEmpty()) {
			return new ResponseEntity<>("Participant list is empty at the moment", HttpStatus.NOT_FOUND);
		}
		Collection<Participant> participants = meeting.getParticipants();
		return new ResponseEntity<Collection<Participant>>(participants, HttpStatus.OK);

	}

	// POST http://localhost:8080/meetings/2/participants/foos?login=user2
	@RequestMapping(value = "/{id}/participants/foos", method = RequestMethod.POST)
	public ResponseEntity<?> assignParticipant(@PathVariable("id") long id, @RequestParam String login) {
		Meeting meeting = meetingService.findByID(id);
		if (meeting == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Collection<Participant> participants = meeting.getParticipants();
		Participant participant = participantService.findByLogin(login);
		if (participant == null) {
			return new ResponseEntity<>("Participant does not exist", HttpStatus.NOT_FOUND);
		}
		if (participants.contains(participant)) {
			return new ResponseEntity<>("Participant is already assigned to the meeting", HttpStatus.CONFLICT);
		}
		meetingService.addParticipant(id, login);
		return new ResponseEntity<Participant>(participant, HttpStatus.OK);
	}

// GOLD	

	// DELETE http://localhost:8080/meetings/2
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteEmptyMeeting(@PathVariable("id") long id) {
		Meeting meeting = meetingService.findByID(id);
		Collection<Participant> participants = meeting.getParticipants();
		if (participants.isEmpty()) {
			meetingService.delete(meeting);
		} else {
			return new ResponseEntity<>("Meetig with participants cannot be deleted", HttpStatus.CONFLICT);
		}
		return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
	}

	// DELETE http://localhost:8080/meetings/2/participants/foos?login=user2
	@RequestMapping(value = "/{id}/participants/foos", method = RequestMethod.DELETE)
	public ResponseEntity<?> removeParticipant(@PathVariable("id") long id, @RequestParam String login) {
		Meeting meeting = meetingService.findByID(id);
		Collection<Participant> participants = meeting.getParticipants();
		Participant participant = participantService.findByLogin(login);
		if (participant == null) {
			return new ResponseEntity<>("Participant does not exist", HttpStatus.NOT_FOUND);
		}
		if (participants.contains(participant) == false) {
			return new ResponseEntity<>("Participant is not assigned to the meeting", HttpStatus.NOT_FOUND);
		}

		meetingService.removeParticipant(id, participant);
		return new ResponseEntity<Participant>(participant, HttpStatus.OK);
	}

	// DELETE http://localhost:8080/meetings/2/participants
	@RequestMapping(value = "/{id}/participants", method = RequestMethod.DELETE)
	public ResponseEntity<?> removeAllParticipants(@PathVariable("id") long id) {
		Meeting meeting = meetingService.findByID(id);
		Collection<Participant> participants = meeting.getParticipants();

		if (participants.isEmpty()) {
			return new ResponseEntity<>("Participant's list is empty", HttpStatus.NOT_FOUND);
		}

		meetingService.removeParticipants(id);

		return new ResponseEntity<Collection<Participant>>(participants, HttpStatus.OK);
	}

	// PUT http://localhost:8080/meetings/2/
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<?> updateParticipant(@PathVariable("id") long id, @RequestBody Meeting meeting) {
		Meeting foundMeeting = meetingService.findByID(id);
		if (foundMeeting == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		foundMeeting.setTitle(meeting.getTitle());
		foundMeeting.setDescription(meeting.getDescription());
		foundMeeting.setDate(meeting.getDate());

		meetingService.update(id);
		return new ResponseEntity<Meeting>(foundMeeting, HttpStatus.OK);
	}

// PREMIUM

	@RequestMapping(value = "/sort", method = RequestMethod.GET)
	// GET http://localhost:8080/meetings/sort?column=title&order=asc
	public ResponseEntity<?> sortMeetings(@RequestParam String column, String order) {
		Collection<Meeting> meetings = meetingService.sortByTitle(column, order);
		return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	// GET http://localhost:8080/meetings/search?title=meet&description=desc
	public ResponseEntity<?> searchMeetings(@RequestParam(required = false) String title, String description) {
		Collection<Meeting> meetings = meetingService.searchForMeetings(title, description);
		if (meetings.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.FOUND);
	}

	@RequestMapping(value = "/search/participant", method = RequestMethod.GET)
	// GET http://localhost:8080/meetings/search/participant?login=user2
	public ResponseEntity<?> searchMeetingsByParticipant(@RequestParam String login) {
		Collection<Meeting> meetings = meetingService.searchByParticipant(login);
		if (meetings.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.FOUND);
	}

}
