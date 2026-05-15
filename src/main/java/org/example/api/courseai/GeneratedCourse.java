package org.example.api.courseai;

import org.example.entities.Chapitres;
import org.example.entities.Cours;

import java.util.List;

public record GeneratedCourse(Cours cours, List<Chapitres> chapitres) {
}
