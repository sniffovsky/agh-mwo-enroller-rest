package com.company.enroller.persistence;

import java.util.Collection;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.company.enroller.exceptions.NoParticipantFoundException;
import com.company.enroller.model.Participant;

@Component("participantService")
public class ParticipantService {

	Session session;

	public ParticipantService() {
		session = DatabaseConnector.getInstance().getSession();
	}

	public Collection<Participant> getAll() {
		return session.createCriteria(Participant.class).list();
	}

	public Participant findByLogin(String login) {
		Participant participant = (Participant) this.session.get(Participant.class, login);
		if (participant==null) {
			throw new NoParticipantFoundException("No participant with login '" + login + "' was found");
		}
		return participant;
	}

	public Participant add(Participant participant) {
		Transaction transaction = this.session.beginTransaction();
		this.session.save(participant);
		transaction.commit();
		return participant;
	}

	public void update(Participant participant) {
		Transaction transaction = this.session.beginTransaction();
		this.session.merge(participant);
		transaction.commit();
	}

	public void delete(Participant participant) {
		Transaction transaction = this.session.beginTransaction();
		this.session.delete(participant);
		transaction.commit();
	}

}
