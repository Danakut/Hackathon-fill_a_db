package cz.danakut.fill_a_db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.sql.Date;

public class FileRecorder implements CourseRecorder {

    private static int courseIdCounter;
    private static String HOME = System.getProperty("user.home");

    @Override
    public int findCourse(Course course) throws Exception {
        Path path = Paths.get(HOME).resolve("Documents/txtdatabase");
        Path file = lookUpNewestFileInDir(path);

        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            //read first line with meta data, so that the line is not processed in the while cycle immediately following
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] courseData = line.split("|");
                Course storedCourse = new Course();
                storedCourse.name = courseData[8];
                storedCourse.startDate = Date.valueOf(courseData[2]);
                storedCourse.startTime = courseData[4];
                storedCourse.quickLocation = courseData[10];

                if (course.equals(storedCourse)) {
                    return storedCourse.id = Integer.parseInt(courseData[0]);
                }
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        return -1;
    }

    @Override
    public void insertCourse(Course course) {
        Path path = Paths.get(HOME).resolve("Documents/txtdatabase/database.txt");
//        Path file = path.resolve(LocalDate.now().toString() + " " + LocalTime.now().toString().replace(".", ":"));
        Charset charset = Charset.forName("UTF-8");
        int courseCount = -1;

        //create new database file
        if (Files.notExists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                System.out.println("File creation failed at path: " + path.toString());
                e.printStackTrace();
            }
            System.out.println("File created with path: " + path.toString());

        //or get the id of the last record in an existing file
        } else {
            try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
                String meta = reader.readLine();
                String[] metas = meta.split("|");
                courseCount = Integer.parseInt(metas[1]);
            } catch (IOException ex) {
                System.err.println("Couldn't read file " + path + ". IOException: " + ex);
            }
        }


        try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
            //TODO dopsat funkcionalitu; v tenhle moment tridu zahazuju, protoze jsem zjistila, ze potrebuju zapisovat do urcitych mist souboru
            // a k tomu potrebuju komplikovanejsi tridu (SeekableByteChannel interface). Vzhledem k tomu, ze textova databaze mela slouzit jen k otestovani,
            //mnozstvi prace vynalozene na dokonceni by neodpovidalo mire uzitecnosti.
            //Zapisovani dat bez overovani predchozich id momentalne funguje bez komplikaci. (Nutno pouzit pokazde zapis do noveho souboru - odkomentovat
            // radek, ktery dava souboru jmeno s aktualnim datem a casem.)

        } catch (IOException x) {
        System.err.format("IOException: %s%n", x);
    }

        String courseString = course.toString();
        try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
            writer.write(courseString);
            writer.newLine();
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    private Path lookUpNewestFileInDir(Path parentDir) throws IOException {
        FileTime lastUpdated = null;
        Path newest = null;
        DirectoryStream<Path> stream = Files.newDirectoryStream(parentDir);
        for (Path file : stream) {
            FileTime filetime = Files.getLastModifiedTime(file);
            if (filetime.compareTo(lastUpdated) > 0) {
                lastUpdated = filetime;
                newest = file;
            }
        }

        return newest;
    }

}
