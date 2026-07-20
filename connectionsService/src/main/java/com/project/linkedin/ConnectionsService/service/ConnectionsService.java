package com.project.linkedin.ConnectionsService.service;

import com.project.linkedin.ConnectionsService.auth.AuthContextHolder;
import com.project.linkedin.ConnectionsService.entity.Person;
import com.project.linkedin.ConnectionsService.exception.BadRequestException;
import com.project.linkedin.ConnectionsService.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConnectionsService {

    private final PersonRepository personRepository;

    public List<Person> getFirstDegreeConnectionsOfUser(Long userId){

        log.info("Getting first degree connections for user with Id : {}",userId);

        return personRepository.getFirstDegreeConnections(userId);

    }

    public void sendConnectionRequest(Long receiverId) {

        Long senderId = AuthContextHolder.getCurrentUserId();
        log.info("Sending Connection Request with sender id {} to reciever id {}",senderId,receiverId);

        if(senderId.equals(receiverId)){
            throw new BadRequestException("Both sender and reciever cannot be same");
        }
        boolean connectionRequestExists = personRepository.connectionRequestExists(senderId,receiverId);
        if(connectionRequestExists){
            throw new BadRequestException("Connection Request already exist");
        }

        boolean alreadyConnected = personRepository.alreadyConnected(senderId,receiverId);
        if(alreadyConnected){
            throw new BadRequestException("Connection already exist");
        }

        personRepository.addConnectionRequest(senderId,receiverId);
        log.info("Connection Request sent successfully");

    }

    public void acceptConnectionRequest(Long senderId) {

        Long receiverId = AuthContextHolder.getCurrentUserId();
        log.info("Accepting Connection Request with sender id {} to reciever id {}",senderId,receiverId);

        if(senderId.equals(receiverId)){
            throw new BadRequestException("Both sender and reciever cannot be same");
        }

        boolean alreadyConnected = personRepository.alreadyConnected(senderId,receiverId);
        if(alreadyConnected){
            throw new BadRequestException("Connection already exist");
        }

        boolean connectionRequestExists = personRepository.connectionRequestExists(senderId,receiverId);
        if(!connectionRequestExists){
            throw new BadRequestException("No Connection Request exists");
        }

        personRepository.acceptConnectionRequest(senderId,receiverId);
        log.info("Connection Request accepted successfully");

    }

    public void rejectConnectionRequest(Long senderId) {

        Long receiverId = AuthContextHolder.getCurrentUserId();
        log.info("Rejecting Connection Request with sender id {} to reciever id {}",senderId,receiverId);

        if(senderId.equals(receiverId)){
            throw new BadRequestException("Both sender and reciever cannot be same");
        }

        boolean connectionRequestExists = personRepository.connectionRequestExists(senderId,receiverId);
        if(!connectionRequestExists){
            throw new BadRequestException("No Connection Request exists");
        }

        personRepository.rejectConnectionRequest(senderId,receiverId);
        log.info("Connection Request rejected successfully");
    }
}
