
package com.example.whatsup.service;

import com.example.whatsup.entity.EmojiType;
import com.example.whatsup.entity.Message;
import com.example.whatsup.entity.Reaction;
import com.example.whatsup.entity.User;
import com.example.whatsup.repository.ReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReactionService {
 private final ReactionRepository reactionRepository;

 @Autowired
 public ReactionService(ReactionRepository reactionRepository) {
  this.reactionRepository = reactionRepository;
 }

 public Reaction addReaction(EmojiType emojiType, Message message, User user) {
  Reaction reaction = new Reaction();
  reaction.setEmojiType(emojiType);
  reaction.setMessage(message);
  reaction.setUser(user);
  return reactionRepository.save(reaction);
 }
}

