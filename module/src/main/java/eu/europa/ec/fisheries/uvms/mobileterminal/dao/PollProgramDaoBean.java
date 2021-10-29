/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.mobileterminal.dao;

import eu.europa.ec.fisheries.uvms.mobileterminal.entity.ProgramPoll;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Stateless
public class PollProgramDaoBean {

    @PersistenceContext
    private EntityManager em;

    public void removeProgramPollAfterTests(String id){
        ProgramPoll pollProgram = getProgramPollById(UUID.fromString(id));
        em.remove(em.contains(pollProgram) ? pollProgram : em.merge(pollProgram));
    }

    public void createProgramPoll(ProgramPoll pollProgram) {
        em.persist(pollProgram);
    }

    public ProgramPoll updateProgramPoll(ProgramPoll pollProgram) {
        pollProgram = em.merge(pollProgram);
        return pollProgram;
    }

    public List<ProgramPoll> getProgramPollsAlive()  {
        TypedQuery<ProgramPoll> query = em.createNamedQuery(ProgramPoll.PROGRAM_FIND_ALIVE, ProgramPoll.class);
        query.setParameter("currentDate", Instant.now());
        return query.getResultList();
    }

    public List<ProgramPoll> getProgramPollRunningAndStarted()  {
        TypedQuery<ProgramPoll> query = em.createNamedQuery(ProgramPoll.PROGRAM_FIND_RUNNING_AND_STARTED, ProgramPoll.class);
        query.setParameter("currentDate", Instant.now());
        return query.getResultList();
    }

    public ProgramPoll getProgramPollByGuid(String guid) {
        return getProgramPollById(UUID.fromString(guid));
    }

    public ProgramPoll getProgramPollById(UUID id) {
        try {
            return em.find(ProgramPoll.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
}
