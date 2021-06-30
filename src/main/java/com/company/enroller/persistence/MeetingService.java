package com.company.enroller.persistence;

import java.util.Collection;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.*;
import org.springframework.stereotype.Component;

import com.company.enroller.model.Meeting;
import com.company.enroller.model.Participant;

@Component("meetingService")
public class MeetingService {

	DatabaseConnector connector;
	Session session;

	public MeetingService() {
		session = DatabaseConnector.getInstance().getSession();
	}

	public Collection<Meeting> getAll() {
		String hql = "FROM Meeting";
		Query query = session.createQuery(hql);
		return query.list();
	}

	public Meeting findByID(long id) {

		return (Meeting) DatabaseConnector.getInstance().getSession().get(Meeting.class, id);
	}

	public void add(Meeting meeting) {
		Transaction transaction = session.beginTransaction();
		session.save(meeting);
		transaction.commit();
	}

	public void addParticipant(Long id, String login) {

		Meeting meeting = this.findByID(id);
		Participant participant = (Participant) DatabaseConnector.getInstance().getSession().get(Participant.class,
				login);

		meeting.addParticipant(participant);
		Transaction transaction = session.beginTransaction();
		session.save(meeting);
		transaction.commit();

	}

	public void delete(Meeting meeting) {
		Transaction transaction = session.beginTransaction();
		session.delete(meeting);
		transaction.commit();
	}

	public void removeParticipant(long id, Participant participant) {
		Meeting meeting = this.findByID(id);
		meeting.removeParticipant(participant);
		Transaction transaction = session.beginTransaction();
		session.save(meeting);
		transaction.commit();

	}

	public void removeParticipants(long id) {
		Meeting meeting = this.findByID(id);
		Collection<Participant> participants = meeting.getParticipants();
		participants.clear();
		Transaction transaction = session.beginTransaction();
		session.save(meeting);
		transaction.commit();
	}

	public void update(long id) {
		Meeting meeting = this.findByID(id);
		Transaction transaction = session.beginTransaction();
		session.merge(meeting);
		transaction.commit();
	}

	public Collection<Meeting> sortByTitle(String column, String order) {
		String hql = "FROM Meeting ORDER BY upper(" + column + ") " + order;
		Query query = session.createQuery(hql);
		return query.list();
	}

	public Collection<Meeting> searchForMeetings(String title, String description) {
		String hql = "FROM Meeting m WHERE m.id in (SELECT id FROM Meeting WHERE upper(Title) like upper('%" + title
				+ "%')) OR m.id in (SELECT id FROM Meeting WHERE upper(Description) like upper('%" + description + "%'))";
		
		Query query = session.createQuery(hql);
		return query.list();
	}

	public Collection<Meeting> searchByParticipant(String login) {
		String hql = "FROM Meeting where id in (SELECT m.id FROM Meeting m JOIN m.participants a WHERE upper(a.login) = upper(:login))";
		
		Query query = session.createQuery(hql);
		query.setString("login", login);
		return query.list();
	}

}
