package com.company.enroller.controllers;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.company.enroller.exceptions.NoParticipantFoundException;
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

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getAll(SpringDataWebProperties.Pageable pageable) {
        Collection<Meeting> meetings = meetingService.getAll();
        return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
    }

    @RequestMapping(value = "/sort/byTitle", method = RequestMethod.GET)
    public ResponseEntity<?> sort() {
        Collection<Meeting> meetings = meetingService.getAll();
        List<Meeting> meetingList = (List<Meeting>) meetings;
        meetingList.sort(Comparator.comparing(Meeting::getTitle));
        return new ResponseEntity<Collection<Meeting>>(meetingList, HttpStatus.OK);
    }

    @RequestMapping(value = "/search/byTitle/{title}", method = RequestMethod.GET)
    public ResponseEntity<?> searchByTitle(@PathVariable("title") String title) {
        Collection<Meeting> meetings = meetingService.getAll();
        List<Meeting> found = meetings.stream().filter(meeting -> meeting.getTitle().contains(title)).collect(Collectors.toList());

        return new ResponseEntity<Collection<Meeting>>(found, HttpStatus.OK);
    }

    @RequestMapping(value = "/search/byParticipant/{participantLogin}", method = RequestMethod.GET)
    public ResponseEntity<?> searchByParticipantName(@PathVariable("participantLogin") String participantLogin) {
        Collection<Meeting> meetings = meetingService.getAll();
        Participant participant = participantService.findByLogin(participantLogin);
        if (participant.equals(null)) {
            throw new NoParticipantFoundException();
        }
        List<Meeting> found = meetings.stream().filter(meeting -> meeting.getParticipants().contains(participant)).collect(Collectors.toList());
        return new ResponseEntity<Collection<Meeting>>(found, HttpStatus.OK);
    }

    @RequestMapping(value = "/search/byDescription/{description}", method = RequestMethod.GET)
    public ResponseEntity<?> searchByDescription(@PathVariable("description") String description) {
        Collection<Meeting> meetings = meetingService.getAll();
        List<Meeting> found = meetings.stream().filter(meeting -> meeting.getDescription().contains(description)).collect(Collectors.toList());

        return new ResponseEntity<Collection<Meeting>>(found, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getMeeting(@PathVariable("id") long id) {
        Meeting meeting = meetingService.findById(id);
        return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteMeeting(@PathVariable("id") long id) {
        Meeting meeting = meetingService.findById(id);
        meetingService.delete(meeting);
        return new ResponseEntity<Meeting>(meeting, HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> addMeeting(@RequestBody Meeting meeting) {
        if (meetingService.alreadyExist(meeting)) {
            return new ResponseEntity<String>(
                    "Unable to add. A meeting with title " + meeting.getTitle() + " and date " + meeting.getDate() + " already exist.", HttpStatus.CONFLICT);
        }
        meetingService.add(meeting);
        return new ResponseEntity<>(meeting, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateMeeting(@PathVariable("id") long id, @RequestBody Meeting meeting) {
        Meeting currentMeeting = meetingService.findById(id);
        meeting.setId(currentMeeting.getId());
        meetingService.update(meeting);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "{id}/participants", method = RequestMethod.GET)
    public ResponseEntity<?> getParticipants(@PathVariable("id") long id) {
        Meeting meeting = meetingService.findById(id);
        return new ResponseEntity<Collection<Participant>>(meeting.getParticipants(), HttpStatus.OK);
    }

    @RequestMapping(value = "{id}/participants", method = RequestMethod.POST)
    public ResponseEntity<?> addParticipant(@PathVariable("id") long id, @RequestBody Map<String, String> json) {

        Meeting currentMeeting = meetingService.findById(id);
        String login = json.get("login");
        if (login == null) {
            return new ResponseEntity<String>("Unable to find participant login in the request body", HttpStatus.BAD_REQUEST);
        }

        Participant participantToAdd = participantService.findByLogin(login);
        currentMeeting.addParticipant(participantToAdd);
        meetingService.update(currentMeeting);

        return new ResponseEntity<Collection<Participant>>(currentMeeting.getParticipants(), HttpStatus.OK);
    }

    @RequestMapping(value = "{id}/participants/{login}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeParticipant(@PathVariable("id") long id, @PathVariable("login") String login) {
        Meeting meeting = meetingService.findById(id);
        Participant participant = participantService.findByLogin(login);
        meeting.removeParticipant(participant);
        meetingService.update(meeting);
        return new ResponseEntity<Collection<Participant>>(meeting.getParticipants(), HttpStatus.OK);
    }

}