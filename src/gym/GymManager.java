package gym;

import constants.Constants;
import enums.Time;
import enums.Location;
import member.Member;
import date.Date;
import member.MemberDatabase;

import java.util.Scanner;


/**
 * This class is the user interface for Gym Manager. The client can input commands via the console / terminal to
 * interact with the database and the fitness classes. See {@link #run() run()} for more details on valid commands.<br><br>
 * **Please Note: There is only one memberDataBase instance.**
 * @author Hasnain Ali, Carolette Saguil
 */
public class GymManager {


    /**
     * Default constructor; not used.
     */
    public GymManager() {

    }

    /**
     * Adds a member to the member database.
     * Fails if: Date of birth is invalid, date of birth is in the future, member is younger than 18 years old, expiration
     * date is invalid, location is invalid, or if member is already in the database.
     * @param lineParts String array for command inputted; allows for easier parsing of command.
     * @param memberDatabase The member database that was created for all current members. Only one exists.
     */
    private void addMember (String[] lineParts, MemberDatabase memberDatabase) {
        Member member = new Member(lineParts[1], lineParts[2], new Date(lineParts[3]), new Date(lineParts[4]),
                Location.returnEnumFromString(lineParts[5]));

        if (!member.getDob().isValid()) {
            System.out.printf("DOB %s: invalid calendar date!\n", member.getDob());
            return;
        } else if (!member.getDob().checkIfDobIsFuture()) {
            System.out.printf("DOB %s: cannot be today or a future date.\n", member.getDob());
            return;
        } else if (!member.getDob().checkMemberAge()) {
            System.out.printf("DOB %s: must be 18 or older to join!\n", member.getDob());
            return;
        } else if (!member.getExpire().isValid()) {
            System.out.printf("Expiration Date %s: invalid calendar date!\n", member.getExpire());
            return;
        } else if (member.getLocation() == null) {
            System.out.printf("%s: invalid location!\n", lineParts[5]);
            return;
        }

        if (memberDatabase.add(member)) {
            System.out.printf("%s %s added.\n", member.getFname(), member.getLname());
        } else {
            System.out.printf("%s %s is already in the database.\n", member.getFname(), member.getLname());
        }
    }

    /**
     * Removes a member from the database.
     * If the member is not in the database, they will fail to be removed. If they are, they will be removed successfully.
     * @param lineParts String array for command inputted; allows for easier parsing of command.
     * @param memberDatabase The member database that was created for all current members.
     */
    private void removeMember(String[] lineParts, MemberDatabase memberDatabase) {
        Member memberToRemove = memberDatabase.getMember(
                memberDatabase.find(lineParts[1], lineParts[2], new Date(lineParts[3])));
        if (memberToRemove == null) {
            System.out.printf("%s %s is not in the database.\n", lineParts[1], lineParts[2]);
            return;
        } else if (memberDatabase.remove(memberToRemove)) {
            System.out.printf("%s %s removed.\n", lineParts[1], lineParts[2]);
            return;
        }

        System.out.printf("%s %s was unable to be removed.\n", lineParts[1], lineParts[2]);
    }

    /**
     * FitnessClass array is instantiated and made here. <br>
     *
     * <ul>
     *     <b>fitnessClass[0] = Pilates, fitnessClass[1] = Spinning, fitnessClass[2] = Cardio</b>
     * </ul>
     * @param fitnessClasses Array consisting of all fitness classes. fitnessClass[0] = Pilates, fitnessClass[1] = Spinning, fitnessClass[2] = Cardio
     */
    private void instantiateFitnessClasses(FitnessClass[] fitnessClasses) {
        fitnessClasses[0] = new FitnessClass(Time.PILATES);
        fitnessClasses[1] = new FitnessClass(Time.SPINNING);
        fitnessClasses[2] = new FitnessClass(Time.CARDIO);
    }

    /**
     * Based on {@code className}, the appropriate index for the class is returned.
     * @param className The name of the class to return the index from.
     * @return The index of the class based on the {@code className}. -1 is the class was not found.
     */
    private int returnClassIndex(String className) {
        if (className.equalsIgnoreCase("pilates")) {
            return Time.PILATES.getClassIndex();
        } else if (className.equalsIgnoreCase("spinning")) {
            return Time.SPINNING.getClassIndex();
        } else if (className.equalsIgnoreCase("cardio")) {
            return Time.CARDIO.getClassIndex();
        } else {
            return Constants.NOT_FOUND;
        }
    }

    /**
     * Checks a member into a particular fitness class. The class must exist, they must have a valid date of birth,
     * exist in the member database, not have an expired membership, not have already checked in, and not have a time conflict with another class.
     * @param lineParts String array for command inputted; allows for easier parsing of command.
     * @param memberDatabase The member database that was created for all current members.
     * @param fitnessClasses Array consisting of all fitness classes. fitnessClass[0] = Pilates, fitnessClass[1] = Spinning, fitnessClass[2] = Cardio
     */
    private void checkInMember (String[] lineParts, MemberDatabase memberDatabase, FitnessClass[] fitnessClasses) {
        int classIndex = returnClassIndex(lineParts[1]);
        String fname = lineParts[2];
        String lname = lineParts[3];
        Date dob = new Date(lineParts[4]);

        if (classIndex == -1) {
            System.out.printf("%s class does not exist.\n", lineParts[1]);
            return;
        } else if (!dob.isValid()) {
            System.out.printf("DOB %s: invalid calendar date!\n", dob);
            return;
        }

        Member memberToCheckIn = memberDatabase.getMember(memberDatabase.find(fname, lname, dob));

        if (memberToCheckIn == null) {
            System.out.printf("%s %s %s does not exist in database.\n", fname, lname, dob);
            return;
        } else if (fitnessClasses[classIndex].checkIfMemberExpired(memberToCheckIn)) {
            System.out.printf("%s %s %s membership expired.\n", fname, lname, memberToCheckIn.getDob());
            return;
        } else if ( !(fitnessClasses[classIndex].checkForTimeConflict(memberToCheckIn)) ) {
            System.out.printf("%s time conflict -- %s %s has already checked into %s\n",
                    fitnessClasses[classIndex].getClassName(), fname, lname, fitnessClasses[classIndex].findTimeConflictClass(memberToCheckIn));
            return;
        } else if ( !(fitnessClasses[classIndex].findMemberInClass(memberToCheckIn) == Constants.NOT_FOUND) ){
            System.out.printf("%s %s has already checked into %s.\n", fname, lname, fitnessClasses[classIndex].getClassName());
            return;
        }

        fitnessClasses[classIndex].checkIn(memberToCheckIn);

        System.out.printf("%s %s checked into %s.\n", memberToCheckIn.getFname(), memberToCheckIn.getLname(), fitnessClasses[classIndex].getClassName());
    }

    /**
     * Drops a particular member from the class. They must be in the class, have a valid date of birth, and be wanting to drop a class that exists.
     * @param lineParts String array for command inputted; allows for easier parsing of command.
     * @param memberDatabase The member database that was created for all current members.
     * @param fitnessClasses Array consisting of all fitness classes. fitnessClass[0] = Pilates, fitnessClass[1] = Spinning, fitnessClass[2] = Cardio
     */
    private void dropClass(String[] lineParts, MemberDatabase memberDatabase, FitnessClass[] fitnessClasses) {
        int classIndex = returnClassIndex(lineParts[1]);
        String fname = lineParts[2];
        String lname = lineParts[3];
        Date dob = new Date(lineParts[4]);

        Member memberToDrop = memberDatabase.getMember(memberDatabase.find(fname, lname, dob));

        if (classIndex == Constants.NOT_FOUND) {
            System.out.printf("%s class does not exist.\n", lineParts[1]);
            return;
        } else if (!dob.isValid()) {
            System.out.printf("DOB %s: invalid calendar date!\n", dob);
            return;
        } else if (memberToDrop == null || fitnessClasses[classIndex].findMemberInClass(memberToDrop) == Constants.NOT_FOUND) {
            System.out.printf("%s %s is not a participant in %s\n", fname, lname, fitnessClasses[classIndex].getClassName());
            return;
        }

        fitnessClasses[classIndex].dropClass(memberToDrop);
        System.out.printf("%s %s dropped %s\n", fname, lname, lineParts[1]);
    }

    /**
     * Starts running the Gym Manager UI blocking while waiting for valid commands. Using the String.split() method,
     * this method parses any input given to it via standard input using Java Scanner. Valid inputs are as follows:
     *
     * <br>
     * <br>
     * <i>***Please Note do not include | in your commands, these are simply here for readability purposes***</i>
     * <br>
     * <br>
     *
     * <ul>
     *     <li><b>A | First_Name | Last_Name | Date_Of_Birth | Expiration_Date | Location</b></li>
     *          <ul>
     *              <li>Appends member to database</li>
     *          </ul>
     *
     *     <li><b>R | First_Name | Last_Name | Date_Of_Birth</b></li>
     *          <ul>
     *              <li>Removes member from the database.</li>
     *          </ul>
     *
     *     <li><b>P</b></li>
     *          <ul>
     *              <li>Print current Member Database</li>
     *          </ul>
     *
     *      <li><b>PC</b></li>
     *      <ul>
     *          <li>Print current Member Database sorted by county</li>
     *      </ul>
     *
     *      <li><b>PN</b></li>
     *      <ul>
     *          <li>Print current Member Database sorted by last name, first name</li>
     *      </ul>
     *
     *      <li><b>PD</b></li>
     *      <ul>
     *          <li>Print current Member Database sorted by membership expiration dates.</li>
     *      </ul>
     *
     *      <li><b>S</b></li>
     *      <ul>
     *          <li>Print fitness class schedule</li>
     *      </ul>
     *
     *      <li><b>C | Class_Name | First_Name | Last_Name | Date_Of_Birth</b></li>
     *      <ul>
     *          <li>Check a person in the database into a particular existing fitness class</li>
     *      </ul>
     *
     *       <li><b>D | Class_Name | First_Name | Last_Name | Date_Of_Birth</b></li>
     *       <ul>
     *           <li>Drops member from a particular fitness class.</li>
     *       </ul>
     *
     *       <li><b>Q</b></li>
     *       <ul>
     *          <li>Quits/Terminates user interface (kills program).</li>
     *       </ul>
     * </ul>
     */
    public void run() {
        System.out.println("Gym Manager Running...");
        Scanner sc = new Scanner(System.in);
        String input;
        MemberDatabase memberDatabase = new MemberDatabase();
        FitnessClass[] fitnessClasses = new FitnessClass[Constants.NUMBER_OF_CLASSES];
        instantiateFitnessClasses(fitnessClasses);

        while ( !((input = sc.nextLine()).equals("Q")) ) {
            String[] lineParts = input.split(" ");

            if (lineParts[0].equals("A")) {
                addMember(lineParts, memberDatabase);
            } else if (lineParts[0].equals("R")) {
                removeMember(lineParts, memberDatabase);
            } else if (lineParts[0].equals("P")) {
                memberDatabase.print();
            } else if (lineParts[0].equals("PC")) {
                memberDatabase.printByCounty();
            } else if (lineParts[0].equals("PN")) {
                memberDatabase.printByName();
            } else if (lineParts[0].equals("PD")) {
                memberDatabase.printByExpirationDate();
            } else if (lineParts[0].equals("S")) {
                FitnessClass.printClassSchedule();
            } else if (lineParts[0].equals("C")) {
                checkInMember(lineParts, memberDatabase, fitnessClasses);
            } else if (lineParts[0].equals("D")) {
                dropClass(lineParts, memberDatabase, fitnessClasses);
            } else if (lineParts[0].equals("")) {
                continue;
            } else {
                System.out.printf("%s is an invalid command!\n", lineParts[0]);
            }

            System.out.println();
        }

        System.out.println("Gym Manager terminated.");
    }
}
