package io.pivotal.pal.tracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@Primary
public class JdbcTimeEntryRepository implements TimeEntryRepository {

    private DataSource dataSource;

    @Autowired
    public JdbcTimeEntryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        try {
            PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement("INSERT into time_entries (project_Id, user_Id, date, hours) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, timeEntry.getProjectId());
            preparedStatement.setLong(2, timeEntry.getUserId());
            preparedStatement.setDate(3, Date.valueOf(timeEntry.getDate()));
            preparedStatement.setInt(4, timeEntry.getHours());
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    timeEntry.setId(generatedKeys.getLong(1));
                    return timeEntry;
                } else {
                    throw new SQLException("Creating time entry failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public TimeEntry find(long id) {
        try {
            PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement("Select * from time_entries where id = ?");
            preparedStatement.setLong(1, id);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                TimeEntry something = TimeEntry.builder()
                        .id(rs.getLong("id"))
                        .projectId(rs.getLong("project_Id"))
                        .userId(rs.getLong("user_Id"))
                        .date(rs.getDate("date").toLocalDate())
                        .hours(rs.getInt("hours"))
                        .build();
                return something;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public List<TimeEntry> list() {
        try {
            PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement("Select * from time_entries");
            ResultSet rs = preparedStatement.executeQuery();
            List<TimeEntry> timeEntries = new ArrayList<>();

            while (rs.next()) {
                TimeEntry timeEntry = TimeEntry.builder()
                        .id(rs.getLong("id"))
                        .projectId(rs.getLong("project_Id"))
                        .userId(rs.getLong("user_Id"))
                        .date(rs.getDate("date").toLocalDate())
                        .hours(rs.getInt("hours"))
                        .build();
                timeEntries.add(timeEntry);
            }
            return timeEntries;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public TimeEntry update(long id, TimeEntry timeEntry) {
        try {
            PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement("UPDATE time_entries SET project_Id  = ?, user_Id = ?, date = ?, hours = ? WHERE id = ?");
            preparedStatement.setLong(1, timeEntry.getProjectId());
            preparedStatement.setLong(2, timeEntry.getUserId());
            preparedStatement.setDate(3, Date.valueOf(timeEntry.getDate()));
            preparedStatement.setInt(4, timeEntry.getHours());
            preparedStatement.setLong(5, id);
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            timeEntry.setId(id);
            return timeEntry;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void delete(long id) {
        try {
            PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement("DELETE from time_entries where id = ?");
            preparedStatement.setLong(1, id);
            preparedStatement.execute();
        } catch (Exception e) {

        }
    }
}
