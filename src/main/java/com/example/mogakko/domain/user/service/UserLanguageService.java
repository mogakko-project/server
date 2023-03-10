package com.example.mogakko.domain.user.service;

import com.example.mogakko.domain.user.domain.User;
import com.example.mogakko.domain.user.domain.UserLanguage;
import com.example.mogakko.domain.user.exception.UserNotFoundException;
import com.example.mogakko.domain.user.repository.UserLanguageRepository;
import com.example.mogakko.domain.user.repository.UserRepository;
import com.example.mogakko.domain.values.domain.Language;
import com.example.mogakko.domain.values.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserLanguageService {

    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final UserLanguageRepository userLanguageRepository;

    @Transactional
    public Long prefer(Long userId, Long languageId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // languageId는 language name으로 찾을것이기 때문에 null이 아님이 보장됨.
        Language language = languageRepository.findById(languageId).get();

        UserLanguage userLanguage = new UserLanguage();
        userLanguage.setUser(user);
        userLanguage.setLanguage(language);

        UserLanguage saveUserLanguage = userLanguageRepository.save(userLanguage);

        return saveUserLanguage.getId();
    }

    public List<UserLanguage> findLanguagesOfUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return userLanguageRepository.findByUser(user);
    }

}
